package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FlameheartWerewolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "169")
public class KessigForgemaster extends Card {

    public KessigForgemaster() {
        setBackFaceCard(new FlameheartWerewolf());

        DealDamageToTargetCreatureEffect damage = new DealDamageToTargetCreatureEffect(1);
        addTrigger(Triggers.blocks(), damage);
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, damage);

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoSpellsCastLastTurn()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "FlameheartWerewolf";
    }
}
