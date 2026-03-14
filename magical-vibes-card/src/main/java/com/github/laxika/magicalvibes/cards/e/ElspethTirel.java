package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "6")
public class ElspethTirel extends Card {

    public ElspethTirel() {
        // +2: You gain 1 life for each creature you control.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new GainLifePerControlledCreatureEffect()),
                "+2: You gain 1 life for each creature you control."
        ));

        // −2: Create three 1/1 white Soldier creature tokens.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateCreatureTokenEffect(
                        3, "Soldier", 1, 1,
                        CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER),
                        Set.of(), Set.of()
                )),
                "\u22122: Create three 1/1 white Soldier creature tokens."
        ));

        // −5: Destroy all other permanents except for lands and tokens.
        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new DestroyAllPermanentsEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())
                        ))
                )),
                "\u22125: Destroy all other permanents except for lands and tokens."
        ));
    }
}
