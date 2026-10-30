package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopSevenAndPerpetuallyGainKeywordsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LookAtTopSevenAndPerpetuallyGainKeywordsEffectHandler implements NormalEffectHandlerBean {

    private static final Set<Keyword> WATCHED_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    private final GameQueryService gameQueryService;
    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopSevenAndPerpetuallyGainKeywordsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }

        List<Card> topSeven = List.copyOf(library.subList(0, Math.min(7, library.size())));
        if (!topSeven.isEmpty()) {
            cardRevealService.revealToPlayer(gameData, entry.getControllerId(),
                    GameEventFact.RevealZone.LIBRARY, topSeven, entry.getControllerId());
        }

        Set<Keyword> foundKeywords = EnumSet.noneOf(Keyword.class);
        Set<CardEffect> protectionEffects = new LinkedHashSet<>();
        for (Card card : topSeven) {
            for (Keyword keyword : card.getKeywords()) {
                if (WATCHED_KEYWORDS.contains(keyword)) {
                    foundKeywords.add(keyword);
                }
            }
            for (CardEffect staticEffect : card.getEffects(EffectSlot.STATIC)) {
                if (staticEffect instanceof ProtectionGrantingEffect protection
                        && protection.protectionScope() == null) {
                    protectionEffects.add(staticEffect);
                }
            }
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, entry.getControllerId());

        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        Set<Keyword> grantableKeywords = EnumSet.noneOf(Keyword.class);
        for (Keyword keyword : foundKeywords) {
            if (!gameQueryService.cantHaveOrGainKeyword(gameData, source, keyword)) {
                grantableKeywords.add(keyword);
            }
        }

        String sourceName = entry.getCard() == null ? "Priest of Possibility" : entry.getCard().getName();
        Card perpetualCard = source.getOriginalCard();
        if (!grantableKeywords.isEmpty()) {
            source.getPersistentGrantedKeywords().addAll(grantableKeywords);
            if (perpetualCard != null) {
                gameData.perpetualKeywords.merge(perpetualCard.getId(), Set.copyOf(grantableKeywords),
                        (existing, added) -> {
                            Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
                            merged.addAll(existing);
                            merged.addAll(added);
                            return Set.copyOf(merged);
                        });
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceName, null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, null,
                            GrantDuration.INDEFINITE, null),
                    sourceId, null, null, EffectDuration.PERMANENT, 0));
        }

        for (CardEffect protectionEffect : protectionEffects) {
            source.addPersistentTriggeredEffect(EffectSlot.STATIC, protectionEffect);
            if (perpetualCard != null) {
                addPerpetualStaticEffect(gameData, perpetualCard.getId(), protectionEffect);
            }
        }
    }

    /**
     * Records a perpetual static effect against the card identity so that every later permanent made from
     * the card carries it, including after it leaves the battlefield and is cast again.
     */
    private void addPerpetualStaticEffect(GameData gameData, UUID cardId, CardEffect effect) {
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            updated.computeIfAbsent(EffectSlot.STATIC, ignoredSlot -> new ArrayList<>()).add(effect);
            updated.replaceAll((slot, slotEffects) -> List.copyOf(slotEffects));
            return Map.copyOf(updated);
        });
    }
}
