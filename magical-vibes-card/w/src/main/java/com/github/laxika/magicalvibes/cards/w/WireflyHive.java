package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "161")
public class WireflyHive extends Card {

    public WireflyHive() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new FlipCoinWinEffect(
                        new CreateTokenEffect("Wirefly", 2, 2, null,
                                List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)),
                        new DestroyAllPermanentsEffect(new PermanentNamedPredicate("Wirefly"))
                )),
                "{3}, {T}: Flip a coin. If you win the flip, create a 2/2 colorless Insect artifact creature token "
                        + "with flying named Wirefly. If you lose the flip, destroy all permanents named Wirefly."
        ));
    }
}
