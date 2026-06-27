package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
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
import java.util.EnumSet;
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                Permanent targetPermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (targetPermanent == null) {
                    log.info("Game {} - Target permanent no longer on battlefield, no token created", gameData.id);
                    return;
                }

                Card sourceCard = targetPermanent.getCard();

                int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
                for (int copy = 0; copy < tokenMultiplier; copy++) {
                    // Create a token that's a copy of the target permanent (copying all copiable values per CR 707.2)
                    Card tokenCard = new Card();
                    tokenCard.setName(sourceCard.getName());
                    tokenCard.setType(sourceCard.getType());
                    tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
                    tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
                    tokenCard.setToken(true);
                    tokenCard.setColor(sourceCard.getColor());
                    tokenCard.setSupertypes(sourceCard.getSupertypes());
                    tokenCard.setPower(sourceCard.getPower());
                    tokenCard.setToughness(sourceCard.getToughness());
                    tokenCard.setSubtypes(sourceCard.getSubtypes());
                    tokenCard.setCardText(sourceCard.getCardText());
                    tokenCard.setSetCode(sourceCard.getSetCode());
                    tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

                    // Copy keywords
                    if (sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) {
                        tokenCard.setKeywords(EnumSet.copyOf(sourceCard.getKeywords()));
                    }

                    // Copy effects and activated abilities (copiable characteristics per CR 707.2)
                    for (EffectSlot slot : EffectSlot.values()) {
                        for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
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
                    log.info("Game {} - Token copy of {} created via Mirrorworks-like ability", gameData.id, sourceCard.getName());

                    // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
                    // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);
                }
    
    }
}
