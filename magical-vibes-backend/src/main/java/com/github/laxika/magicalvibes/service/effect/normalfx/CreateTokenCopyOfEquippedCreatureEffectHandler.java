package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEquippedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEquippedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfEquippedCreatureEffect) effect;
        
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (sourcePermanent == null) {
                    log.info("Game {} - Source equipment no longer on battlefield", gameData.id);
                    return;
                }

                UUID equippedCreatureId = sourcePermanent.getAttachedTo();
                if (equippedCreatureId == null) {
                    log.info("Game {} - Equipment is not attached to any creature", gameData.id);
                    return;
                }

                Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equippedCreatureId);
                if (equippedCreature == null) {
                    log.info("Game {} - Equipped creature no longer on battlefield", gameData.id);
                    return;
                }

                Card sourceCard = equippedCreature.getCard();

                int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
                for (int copy = 0; copy < tokenMultiplier; copy++) {
                    // Create a token that's a copy of the equipped creature (copying all copiable values per CR 707.2)
                    Card tokenCard = new Card();
                    tokenCard.setName(sourceCard.getName());
                    tokenCard.setType(sourceCard.getType());
                    tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
                    tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
                    tokenCard.setToken(true);
                    tokenCard.setColor(sourceCard.getColor());
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

                    // Copy keywords, optionally adding haste
                    if (sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) {
                        EnumSet<Keyword> tokenKeywords = EnumSet.copyOf(sourceCard.getKeywords());
                        if (e.grantHaste()) {
                            tokenKeywords.add(Keyword.HASTE);
                        }
                        tokenCard.setKeywords(tokenKeywords);
                    } else if (e.grantHaste()) {
                        tokenCard.setKeywords(EnumSet.of(Keyword.HASTE));
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

                    String logMsg = "A token copy of " + sourceCard.getName() + " is created (non-legendary, with haste).";
                    gameBroadcastService.logAndBroadcast(gameData, logMsg);
                    log.info("Game {} - Token copy of {} created via Helm of the Host", gameData.id, sourceCard.getName());

                    // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
                    // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);
                }
    
    }
}
