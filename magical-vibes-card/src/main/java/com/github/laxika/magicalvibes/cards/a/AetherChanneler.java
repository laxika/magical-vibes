package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "42")
public class AetherChanneler extends Card {

    public AetherChanneler() {
        PermanentPredicate anotherNonlandPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 white Bird creature token with flying",
                        new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Return another target nonland permanent to its owner's hand",
                        ReturnToHandEffect.target(),
                        new PermanentPredicateTargetFilter(
                                anotherNonlandPermanent,
                                "Target must be another nonland permanent")),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect())
        )));
    }
}
