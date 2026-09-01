package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "238")
public class CastleArdenvale extends Card {

    public CastleArdenvale() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PLAINS))),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{W}",
                List.of(new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of())),
                "{2}{W}{W}, {T}: Create a 1/1 white Human creature token."
        ));
    }
}
