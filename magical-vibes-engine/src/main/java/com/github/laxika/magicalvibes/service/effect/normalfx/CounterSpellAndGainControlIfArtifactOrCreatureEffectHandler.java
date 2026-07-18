package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndGainControlIfArtifactOrCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterSpellAndGainControlIfArtifactOrCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndGainControlIfArtifactOrCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID ownerId = targetEntry.getControllerId();
        Card gained = counterSupport.counterSpellGainingArtifactOrCreatureControl(gameData, entry, targetEntry);
        if (gained == null) return;

        UUID controllerId = entry.getControllerId();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(gained);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        // Owner is the countered spell's caster; a non-owner controller needs the ownership record and
        // an indefinite control effect so cleanup reconciliation does not revert control to the owner.
        if (!controllerId.equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, ownerId);
        }
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, gained);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(gameData.playerIdToName.get(controllerId) + " puts ", gained, " onto the battlefield under their control."));
    }
}
