package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
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
public class CreateTokenCopyOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfSourceEffect) effect;
        
                // Try to get the source card from the battlefield; if the source left (e.g. planeswalker
                // at 0 loyalty after paying cost), fall back to the card stored on the stack entry
                // (CR 608.2b: abilities resolve even if the source has left the zone).
                Card sourceCard;
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (sourcePermanent != null) {
                    sourceCard = sourcePermanent.getCard();
                } else {
                    sourceCard = entry.getCard();
                    if (sourceCard == null) {
                        log.info("Game {} - Source permanent no longer on battlefield and no card reference", gameData.id);
                        return;
                    }
                }

                int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
                int totalAmount = e.amount() * tokenMultiplier;
                for (int copy = 0; copy < totalAmount; copy++) {
                    // Create a token that's a copy of the source permanent (copying all copiable values per CR 707.2)
                    Card tokenCard = new Card();
                    tokenCard.setName(sourceCard.getName());
                    tokenCard.setType(sourceCard.getType());
                    tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
                    tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
                    tokenCard.setToken(true);
                    tokenCard.setColor(sourceCard.getColor());
                    tokenCard.setLoyalty(sourceCard.getLoyalty());
                    tokenCard.setPower(sourceCard.getPower());
                    tokenCard.setToughness(sourceCard.getToughness());
                    tokenCard.setSubtypes(sourceCard.getSubtypes());
                    tokenCard.setCardText(sourceCard.getCardText());
                    tokenCard.setSetCode(sourceCard.getSetCode());
                    tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

                    // Handle supertypes: optionally remove LEGENDARY
                    if (e.removeLegendary() && sourceCard.getSupertypes().contains(CardSupertype.LEGENDARY)) {
                        EnumSet<CardSupertype> modifiedSupertypes = EnumSet.copyOf(sourceCard.getSupertypes());
                        modifiedSupertypes.remove(CardSupertype.LEGENDARY);
                        tokenCard.setSupertypes(modifiedSupertypes);
                    } else {
                        tokenCard.setSupertypes(sourceCard.getSupertypes());
                    }

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

                    // Planeswalker tokens enter with loyalty counters and no summoning sickness
                    if (tokenCard.getType() == CardType.PLANESWALKER) {
                        tokenPermanent.setCounterCount(CounterType.LOYALTY, tokenCard.getLoyalty() != null ? tokenCard.getLoyalty() : 0);
                        tokenPermanent.setSummoningSick(false);
                    }

                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

                    String logMsg = e.removeLegendary()
                            ? "A non-legendary token copy of " + sourceCard.getName() + " is created."
                            : "A token copy of " + sourceCard.getName() + " is created.";
                    gameBroadcastService.logAndBroadcast(gameData, logMsg);
                    log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(), sourceCard.getName());

                    // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
                    // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);
                }
    
    }
}
