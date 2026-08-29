package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "161")
public class SynodCenturion extends Card {

    public SynodCenturion() {
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                    if (battlefield == null) return true;
                    return battlefield.stream()
                            .filter(permanent -> !permanent.getId().equals(sourcePermanent.getId()))
                            .noneMatch(permanent -> permanent.getCard().hasType(CardType.ARTIFACT));
                },
                List.of(new SacrificeSelfEffect()),
                "Synod Centurion's state-triggered ability"
        ));
    }
}
