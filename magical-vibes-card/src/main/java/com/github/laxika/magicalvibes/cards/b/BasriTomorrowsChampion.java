package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "3")
public class BasriTomorrowsChampion extends Card {

    public BasriTomorrowsChampion() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new CreateTokenEffect(
                                "Cat", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.CAT), Set.of(Keyword.LIFELINK), Set.of()
                        )
                ),
                "{W}, {T}, Exert Basri: Create a 1/1 white Cat creature token with lifelink."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new GrantKeywordEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE),
                                GrantScope.OWN_CREATURES,
                                new PermanentHasSubtypePredicate(CardSubtype.CAT)
                        ),
                        new DrawCardEffect(1)
                ),
                "Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.)"
        ));
    }
}
