package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "44")
public class RimewindTaskmage extends Card {

    public RimewindTaskmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MayEffect(
                        new TapOrUntapTargetPermanentEffect(),
                        "You may tap or untap target permanent?")),
                "{1}, {T}: You may tap or untap target permanent. Activate only if you control four or more snow permanents.",
                TargetFilters.permanent()
        ).withRequiredControlledPermanents(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                4,
                "four or more snow permanents"));
    }
}
