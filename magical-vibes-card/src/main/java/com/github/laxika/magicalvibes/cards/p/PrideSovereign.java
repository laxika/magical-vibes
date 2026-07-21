package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "126")
public class PrideSovereign extends Card {

    public PrideSovereign() {
        // This creature gets +1/+1 for each other Cat you control.
        PermanentCount otherCats = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CAT),
                CountScope.CONTROLLER, true);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(otherCats, otherCats));

        // {W}, {T}, Exert this creature: Create two 1/1 white Cat creature tokens with lifelink.
        addActivatedAbility(new ActivatedAbility(
                true, "{W}",
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new CreateTokenEffect(
                                2, "Cat", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.CAT),
                                Set.of(Keyword.LIFELINK), Set.of()
                        )
                ),
                "{W}, {T}, Exert this creature: Create two 1/1 white Cat creature tokens with lifelink."
        ));
    }
}
