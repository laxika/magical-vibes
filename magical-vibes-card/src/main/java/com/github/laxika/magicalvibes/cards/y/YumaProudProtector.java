package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "4")
public class YumaProudProtector extends Card {

    private static final MayEffect SACRIFICE_LAND_TO_DRAW = new MayEffect(
            new SacrificePermanentThenEffect(
                    new PermanentIsLandPredicate(),
                    new DrawCardEffect(1),
                    "a land",
                    false,
                    false),
            "Sacrifice a land to draw a card?");

    public YumaProudProtector() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SACRIFICE_LAND_TO_DRAW);
        addEffect(EffectSlot.ON_ATTACK, SACRIFICE_LAND_TO_DRAW);

        addEffect(EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DESERT),
                        new CreateTokenEffect(
                                1,
                                "Plant Warrior",
                                4,
                                2,
                                CardColor.GREEN,
                                List.of(CardSubtype.PLANT, CardSubtype.WARRIOR),
                                Set.of(Keyword.REACH),
                                Set.of())));
    }
}
