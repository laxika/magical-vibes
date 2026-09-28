package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongAttachedEquipment;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "44")
@CardRegistration(set = "DMC", collectorNumber = "66")
public class TetsuoImperialChampion extends Card {

    public TetsuoImperialChampion() {
        var greatestEquipmentManaValue = new GreatestManaValueAmongAttachedEquipment();
        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new Equipped(),
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Tetsuo deals damage equal to the greatest mana value among Equipment attached to it to any target",
                                new DealDamageToAnyTargetEffect(greatestEquipmentManaValue)),
                        new ChooseOneEffect.ChooseOneOption(
                                "You may cast an instant or sorcery spell from your hand with mana value less than or equal to the greatest mana value among Equipment attached to Tetsuo without paying its mana cost",
                                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                                        instantOrSorcery, greatestEquipmentManaValue))
                ))));
    }
}
