package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "49")
public class OmnispellAdept extends Card {

    public OmnispellAdept() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))),
                "{2}{U}, {T}: You may cast an instant or sorcery spell from your hand without paying its mana cost."
        ));
    }
}
