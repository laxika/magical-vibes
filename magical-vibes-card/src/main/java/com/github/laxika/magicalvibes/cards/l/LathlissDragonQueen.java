package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "149")
public class LathlissDragonQueen extends Card {

    public LathlissDragonQueen() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        new CreateTokenEffect("Dragon", 5, 5, CardColor.RED,
                                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                "{1}{R}: Dragons you control get +1/+0 until end of turn."));
    }
}
