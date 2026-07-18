package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachControlledCreatureTokenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachControlledCreatureTokenEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachControlledCreatureTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        // Snapshot the creature tokens first so the copies we create aren't themselves copied.
        List<Card> sourceCards = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().isToken() && gameQueryService.isCreature(gameData, permanent)) {
                sourceCards.add(permanent.getCard());
            }
        }

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (Card sourceCard : sourceCards) {
            for (int copy = 0; copy < tokenMultiplier; copy++) {
                createTokenCopy(gameData, entry, sourceCard);
            }
        }
    }

    private void createTokenCopy(GameData gameData, StackEntry entry, Card sourceCard) {
        // Copy all copiable characteristics per CR 707.2.
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
        tokenCard.setSubtypes(sourceCard.getSubtypes() != null ? new ArrayList<>(sourceCard.getSubtypes()) : null);
        tokenCard.setCardText(sourceCard.getCardText());
        tokenCard.setSetCode(sourceCard.getSetCode());
        tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

        if (sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) {
            tokenCard.setKeywords(EnumSet.copyOf(sourceCard.getKeywords()));
        }

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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
        log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(),
                entry.getCard() != null ? entry.getCard().getName() : "ability");

        // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
        // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);
    }
}
