package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "196")
public class BonnyPallClearcutter extends Card {

    public BonnyPallClearcutter() {
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        CreateTokenEffect beau = new CreateTokenEffect(
                CardType.CREATURE, 1, "Beau", 0, 0, CardColor.BLUE, null,
                List.of(CardSubtype.OX), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                        landsYouControl, landsYouControl)),
                List.of(), false, false, true, 0, Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, beau);
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new PutCardFromHandOrGraveyardOntoBattlefieldEffect(
                                new CardTypePredicate(CardType.LAND), "land")));
    }
}
