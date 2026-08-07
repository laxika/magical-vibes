package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.b.BudokaGardener}.
 */
public class DokaiWeaverOfLife extends Card {

    public DokaiWeaverOfLife() {
        // "{4}{G}{G}, {T}: Create an X/X green Elemental creature token, where X is the number of lands
        // you control." - X is counted as the token is created, so the same amount feeds both P/T.
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G}{G}",
                List.of(new CreateTokenEffect(
                        "Elemental",
                        landsYouControl,
                        landsYouControl,
                        CardColor.GREEN,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(),
                        Set.of()
                )),
                "{4}{G}{G}, {T}: Create an X/X green Elemental creature token, where X is the number of "
                        + "lands you control."
        ));
    }
}
