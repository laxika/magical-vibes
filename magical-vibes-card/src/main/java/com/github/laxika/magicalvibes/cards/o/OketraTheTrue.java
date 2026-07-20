package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "21")
public class OketraTheTrue extends Card {

    public OketraTheTrue() {
        // Oketra can't attack or block unless you control at least three other creatures.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new ControlsOtherPermanentCount(3, new PermanentIsCreaturePredicate()),
                "you control at least three other creatures"
        ));

        // {3}{W}: Create a 1/1 white Warrior creature token with vigilance.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{W}",
                List.of(new CreateTokenEffect("Warrior", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.WARRIOR), Set.of(Keyword.VIGILANCE), Set.of())),
                "{3}{W}: Create a 1/1 white Warrior creature token with vigilance."
        ));
    }
}
