package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerMayPayManaOrLifeEffect;

@CardRegistration(set = "MIR", collectorNumber = "172")
public class EmberwildeDjinn extends Card {

    public EmberwildeDjinn() {
        // At the beginning of each player's upkeep, that player may pay {R}{R} or 2 life. If the
        // player does, they gain control of this creature. EACH_UPKEEP_TRIGGERED puts the active
        // player on the trigger's targetId, which is both who is asked to pay and who gains control.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new TargetPlayerMayPayManaOrLifeEffect(
                "{R}{R}", 2, new TargetPlayerGainsControlOfSourceCreatureEffect(),
                "pay {R}{R} or 2 life to gain control of Emberwilde Djinn"));
    }
}
