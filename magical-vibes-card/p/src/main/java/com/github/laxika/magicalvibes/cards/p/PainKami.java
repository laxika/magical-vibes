package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "183")
public class PainKami extends Card {

    public PainKami() {
        // {X}{R}, Sacrifice this creature: It deals X damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(new XValue())),
                "{X}{R}, Sacrifice Pain Kami: It deals X damage to target creature."
        ));
    }
}
