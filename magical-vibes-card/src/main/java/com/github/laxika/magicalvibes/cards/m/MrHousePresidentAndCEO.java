package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "7")
@CardRegistration(set = "PIP", collectorNumber = "421")
@CardRegistration(set = "PIP", collectorNumber = "535")
@CardRegistration(set = "PIP", collectorNumber = "949")
public class MrHousePresidentAndCEO extends Card {

    public MrHousePresidentAndCEO() {
        CreateTokenEffect robot = new CreateTokenEffect(
                "Robot", 3, 3, null,
                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT));

        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                ConditionalEffect.unless(
                        new EventValueAtLeast(4),
                        SequenceEffect.of(
                                robot,
                                ConditionalEffect.unless(
                                        new EventValueAtLeast(6),
                                        CreateTokenEffect.ofTreasureToken(1)))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new RollD6Effect(new Sum(new Fixed(1), new TreasureManaSpentToActivate()))),
                "{4}, {T}: Roll a six-sided die plus an additional six-sided die for each mana from Treasures spent to activate this ability."
        ));
    }
}
