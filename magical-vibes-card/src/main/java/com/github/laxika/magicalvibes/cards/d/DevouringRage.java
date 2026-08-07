package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "164")
public class DevouringRage extends Card {

    public DevouringRage() {
        // As an additional cost to cast this spell, you may sacrifice any number of Spirits — the
        // count sacrificed this way snapshots into the spell's X value.
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsCost(
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
        // Target creature gets +3/+0, plus an additional +3/+0 for each Spirit sacrificed this way.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Sum(new Fixed(3), new Scaled(new XValue(), 3)),
                new Fixed(0)
        ));
    }
}
