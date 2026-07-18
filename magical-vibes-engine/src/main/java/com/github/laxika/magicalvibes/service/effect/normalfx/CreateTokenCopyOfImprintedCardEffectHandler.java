package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfImprintedCardEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfImprintedCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfImprintedCardEffect) effect;
        
                // Per rulings (Mimic Vat, Prototype Portal): if the source permanent has left the battlefield
                // by the time the ability resolves, the token is still created. The imprinted card reference
                // is preserved via entry.getCard() (a snapshot captured when the ability went on the stack).
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

                Card imprintedCard = sourcePermanent != null
                        ? gameData.getImprintedCard(sourcePermanent.getCard())
                        : gameData.getImprintedCard(entry.getCard());
                if (imprintedCard == null) {
                    log.info("Game {} - No card imprinted on {}, no token created", gameData.id, entry.getCard().getName());
                    return;
                }

                int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
                for (int copy = 0; copy < tokenMultiplier; copy++) {
                    // Create a token that's a copy of the imprinted card (copying all copiable values)
                    Card tokenCard = new Card();
                    tokenCard.setName(imprintedCard.getName());
                    tokenCard.setType(imprintedCard.getType());
                    tokenCard.setAdditionalTypes(imprintedCard.getAdditionalTypes());
                    tokenCard.setManaCost(imprintedCard.getManaCost() != null ? imprintedCard.getManaCost() : "");
                    tokenCard.setToken(true);
                    tokenCard.setColor(imprintedCard.getColor());
                    tokenCard.setSupertypes(imprintedCard.getSupertypes());
                    tokenCard.setPower(imprintedCard.getPower());
                    tokenCard.setToughness(imprintedCard.getToughness());
                    tokenCard.setSubtypes(imprintedCard.getSubtypes());
                    tokenCard.setCardText(imprintedCard.getCardText());
                    tokenCard.setSetCode(imprintedCard.getSetCode());
                    tokenCard.setCollectorNumber(imprintedCard.getCollectorNumber());

                    // Copy keywords (conditionally add haste)
                    Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
                    if (imprintedCard.getKeywords() != null) {
                        keywords.addAll(imprintedCard.getKeywords());
                    }
                    if (e.grantHaste()) {
                        keywords.add(Keyword.HASTE);
                    }
                    tokenCard.setKeywords(keywords);

                    // Copy effects and activated abilities (copiable characteristics per CR 707.2)
                    for (EffectSlot slot : EffectSlot.values()) {
                        for (EffectRegistration reg : imprintedCard.getEffectRegistrations(slot)) {
                            tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                        }
                    }
                    for (ActivatedAbility ability : imprintedCard.getActivatedAbilities()) {
                        tokenCard.addActivatedAbility(ability);
                    }
                    tokenCard.copyTargetingFrom(imprintedCard);

                    Permanent tokenPermanent = new Permanent(tokenCard);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

                    // Conditionally schedule for exile at beginning of next end step
                    if (e.exileAtEndStep()) {
                        gameData.queueDelayedAction(new DelayedPermanentAction(tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
                    }

                    String logMsg = e.grantHaste()
                            ? "A token copy of " + imprintedCard.getName() + " is created with haste."
                            : "A token copy of " + imprintedCard.getName() + " is created.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
                    log.info("Game {} - Token copy of {} created via {}", gameData.id, imprintedCard.getName(), sourcePermanent.getCard().getName());

                    // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
                    // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);
                }
    
    }
}
