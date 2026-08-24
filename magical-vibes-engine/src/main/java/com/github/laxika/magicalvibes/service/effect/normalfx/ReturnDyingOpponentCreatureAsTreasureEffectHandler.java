package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureAsTreasureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnDyingOpponentCreatureAsTreasureEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingOpponentCreatureAsTreasureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var treasureEffect = (ReturnDyingOpponentCreatureAsTreasureEffect) effect;
        UUID controllerId = entry.getControllerId();

        entry.setTargetId(treasureEffect.dyingCardId());
        GraveyardReturnSupport.StolenCreatureResult result =
                graveyardReturnSupport.stealFromOpponentGraveyard(gameData, entry, controllerId);
        if (result == null) {
            return;
        }

        Card treasureCard = result.card().createRuntimeCopy();
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setAdditionalTypes(Set.of());
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.freeze();

        Permanent permanent = result.permanent();
        permanent.setCard(treasureCard);
        permanent.tap();

        addTreasureAbilityEffects(gameData, entry, permanent);

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
        graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, result.originalOwnerId());

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " returns ", result.card(),
                " to the battlefield under their control as a Treasure."));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, result.card());
    }

    private void addTreasureAbilityEffects(GameData gameData, StackEntry entry, Permanent permanent) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new LosesAllAbilitiesEffect(GrantScope.TARGET), permanent.getId(), null, null,
                EffectDuration.PERMANENT, 0));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantActivatedAbilityEffect(new ActivatedAbility(
                        true, null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "{T}, Sacrifice this artifact: Add one mana of any color."), GrantScope.TARGET),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }
}
