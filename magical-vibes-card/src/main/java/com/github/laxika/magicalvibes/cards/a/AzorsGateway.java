package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SanctumOfTheSun;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.condition.SourceExiledDifferentManaValuesThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "176")
public class AzorsGateway extends Card {

    public AzorsGateway() {
        setBackFaceCard(new SanctumOfTheSun());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DrawCardEffect(1),
                        new ExileCardFromHandWithSourceEffect(),
                        new ConditionalEffect(
                                new SourceExiledDifferentManaValuesThreshold(5),
                                SequenceEffect.of(
                                        new GainLifeEffect(5),
                                        new UntapPermanentsEffect(TapUntapScope.SELF),
                                        new TransformSelfEffect()
                                )
                        )
                ),
                "{1}, {T}: Draw a card, then exile a card from your hand. If cards with five or more different mana values are exiled with Azor's Gateway, you gain 5 life, untap Azor's Gateway, and transform it."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SanctumOfTheSun";
    }
}
