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
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopSevenAndPerpetuallyGainKeywordsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
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

        Set<Keyword> foundKeywords = EnumSet.noneOf(Keyword.class);
        Set<CardEffect> protectionEffects = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(7, library.size()); i++) {
            Card card = library.get(i);
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
        if (!grantableKeywords.isEmpty()) {
            source.getPersistentGrantedKeywords().addAll(grantableKeywords);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceName, null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET, null,
                            GrantDuration.INDEFINITE, null),
                    sourceId, null, null, EffectDuration.PERMANENT, 0));
        }

        for (CardEffect protectionEffect : protectionEffects) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceName, null, entry.getControllerId(),
                    new GrantEffectEffect(protectionEffect, GrantScope.TARGET),
                    sourceId, null, null, EffectDuration.PERMANENT, 0));
        }
    }
}
