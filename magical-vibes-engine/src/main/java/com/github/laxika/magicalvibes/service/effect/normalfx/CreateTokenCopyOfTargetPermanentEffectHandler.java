package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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
        boolean hasPTOverride = e.powerOverride() != null || e.toughnessOverride() != null;

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
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

            if (sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) {
                tokenCard.setKeywords(EnumSet.copyOf(sourceCard.getKeywords()));
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

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

            String logMsg = "A token copy of " + sourceCard.getName() + " is created.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
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
}
