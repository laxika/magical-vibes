package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "22")
@CardRegistration(set = "PIP", collectorNumber = "370")
@CardRegistration(set = "PIP", collectorNumber = "550")
@CardRegistration(set = "PIP", collectorNumber = "898")
public class ThePrydwenSteelFlagship extends Card {

    public ThePrydwenSteelFlagship() {
        CreateTokenEffect humanKnight = new CreateTokenEffect(
                1, "Human Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT), Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new ConditionalEffect(
                        new PermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 1),
                        new BoostSelfEffect(2, 2))));

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, humanKnight);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
