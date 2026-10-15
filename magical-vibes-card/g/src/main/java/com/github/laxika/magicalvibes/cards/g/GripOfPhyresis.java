package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C16", collectorNumber = "9")
@CardRegistration(set = "CM2", collectorNumber = "43")
public class GripOfPhyresis extends Card {

    public GripOfPhyresis() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment"
        )).addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                new CreateTokenEffect("Phyrexian Germ", 0, 0, CardColor.BLACK,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.GERM), Set.of(), Set.of()),
                new AttachTargetEquipmentToCreatedPermanentEffect()));
    }
}
