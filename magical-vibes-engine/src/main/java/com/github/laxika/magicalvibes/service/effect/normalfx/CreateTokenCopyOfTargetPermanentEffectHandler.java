package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ExileTokenAtEndStep;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfTargetPermanentEffect) effect;

        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (targetPermanent == null) {
            log.info("Game {} - Target permanent no longer on battlefield, no token created", gameData.id);
            return;
        }

        Card sourceCard = targetPermanent.getCard();

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = buildTokenCopyCard(sourceCard, e);
            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

            if (e.exileAtEndStep()) {
                gameData.queueDelayedAction(new ExileTokenAtEndStep(tokenPermanent.getId()));
            }
            if (e.sacrificeAtEndStep()) {
                gameData.queueDelayedAction(new SacrificeAtEndStep(tokenPermanent.getId()));
            }

            String logMsg = "A token copy of " + sourceCard.getName() + " is created.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(),
                    entry.getCard().getName());

            // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
            // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);

            if (e.initialCounters() != null && !e.initialCounters().isEmpty()
                    && !gameQueryService.cantHaveCounters(gameData, tokenPermanent)) {
                for (var counterEntry : e.initialCounters().entrySet()) {
                    if (counterEntry.getValue() > 0) {
                        permanentCounterSupport.placeCounterOnPermanent(
                                gameData, entry, tokenPermanent, counterEntry.getKey(), counterEntry.getValue());
                    }
                }
            }
        }
    }

    /**
     * Builds an unfrozen token {@link Card} that copies {@code sourceCard}'s copiable characteristics
     * (CR 707.2), applying the effect's optional overrides (P/T, additional subtypes/types, haste).
     * Shared with {@code CreateTokenCopyAndLinkToSourceEffectHandler}, which builds the same copy and
     * then attaches its own leaves-battlefield trigger before wrapping the card in a Permanent.
     */
    static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect e) {
        boolean hasPTOverride = e.powerOverride() != null || e.toughnessOverride() != null;

        Card tokenCard = new Card();
        tokenCard.setName(sourceCard.getName());
        tokenCard.setType(sourceCard.getType());
        tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
        tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
        tokenCard.setToken(true);
        tokenCard.setColor(sourceCard.getColor());
        tokenCard.setSupertypes(sourceCard.getSupertypes());
        tokenCard.setPower(e.powerOverride() != null ? e.powerOverride() : sourceCard.getPower());
        tokenCard.setToughness(e.toughnessOverride() != null ? e.toughnessOverride() : sourceCard.getToughness());
        tokenCard.setCardText(sourceCard.getCardText());
        tokenCard.setSetCode(sourceCard.getSetCode());
        tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

        List<CardSubtype> subtypes = new ArrayList<>();
        if (sourceCard.getSubtypes() != null) {
            subtypes.addAll(sourceCard.getSubtypes());
        }
        if (e.additionalSubtypes() != null) {
            for (CardSubtype subtype : e.additionalSubtypes()) {
                if (!subtypes.contains(subtype)) {
                    subtypes.add(subtype);
                }
            }
        }
        tokenCard.setSubtypes(subtypes);

        if (e.additionalTypes() != null && !e.additionalTypes().isEmpty()) {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(tokenCard.getAdditionalTypes());
            for (CardType additionalType : e.additionalTypes()) {
                if (additionalType != tokenCard.getType() && !merged.contains(additionalType)) {
                    merged.add(additionalType);
                }
            }
            tokenCard.setAdditionalTypes(merged);
        }

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (sourceCard.getKeywords() != null) {
            keywords.addAll(sourceCard.getKeywords());
        }
        if (e.grantHaste()) {
            keywords.add(Keyword.HASTE);
        }
        if (!keywords.isEmpty()) {
            tokenCard.setKeywords(keywords);
        }

        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
                // CR 707.9d: when a copy effect provides specific P/T values,
                // characteristic-defining abilities that define P/T are not copied
                if (hasPTOverride && reg.effect().isPowerToughnessDefining()) {
                    continue;
                }
                tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }
        for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
            tokenCard.addActivatedAbility(ability);
        }
        tokenCard.copyTargetingFrom(sourceCard);

        return tokenCard;
    }
}
