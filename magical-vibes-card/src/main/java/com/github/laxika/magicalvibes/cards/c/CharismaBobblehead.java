package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "130")
@CardRegistration(set = "PIP", collectorNumber = "658")
@CardRegistration(set = "PIP", collectorNumber = "1060")
public class CharismaBobblehead extends Card {

    public CharismaBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {4}, {T}: Create X 1/1 white Soldier creature tokens, where X is the number of
        // Bobbleheads you control. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new CreateTokenEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.BOBBLEHEAD),
                                CountScope.CONTROLLER),
                        "Soldier",
                        1,
                        1,
                        CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER),
                        Set.of(),
                        Set.of())),
                "{4}, {T}: Create X 1/1 white Soldier creature tokens, where X is the number of "
                        + "Bobbleheads you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
