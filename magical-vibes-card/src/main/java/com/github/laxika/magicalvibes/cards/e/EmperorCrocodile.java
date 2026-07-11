package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "241")
public class EmperorCrocodile extends Card {

    public EmperorCrocodile() {
        // "When you control no other creatures, sacrifice this creature." —
        // State-triggered ability (MTG rule 603.8). The crocodile itself is excluded.
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                    if (battlefield == null) return true;
                    return battlefield.stream()
                            .filter(p -> !p.getId().equals(sourcePermanent.getId()))
                            .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
                },
                List.of(new SacrificeSelfEffect()),
                "Emperor Crocodile's state-triggered ability"
        ));
    }
}
