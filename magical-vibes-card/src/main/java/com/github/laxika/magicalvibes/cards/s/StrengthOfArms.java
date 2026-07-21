package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "42")
public class StrengthOfArms extends Card {

    public StrengthOfArms() {
        // Target creature gets +2/+2 until end of turn.
        // If you control an Equipment, create a 1/1 white Human Soldier creature token.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)),
                new CreateTokenEffect("Human Soldier", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of())));
    }
}
