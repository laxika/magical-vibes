package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "711")
@CardRegistration(set = "CMM", collectorNumber = "776")
public class RukarumelBiologist extends Card {

    public RukarumelBiologist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        addEffect(EffectSlot.STATIC, new GrantChosenSubtypeToOwnCreaturesEffect(
                true,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER)
                ))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CreateTokenEffect(
                        "Sliver", 1, 1, null,
                        List.of(CardSubtype.SLIVER), Set.of(), Set.of())),
                "{3}, {T}: Create a 1/1 colorless Sliver creature token."
        ));
    }
}
