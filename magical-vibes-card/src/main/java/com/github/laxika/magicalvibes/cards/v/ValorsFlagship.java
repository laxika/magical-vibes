package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "35")
public class ValorsFlagship extends Card {

    public ValorsFlagship() {
        CreateTokenEffect pilot = new CreateTokenEffect(new XValue(), "Pilot", 1, 1, null,
                List.of(CardSubtype.PILOT), Set.of(), Set.of())
                .withTokenEffects(Map.of(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2)));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{2}{W}",
                List.of(pilot, new DrawCardEffect(1)),
                "Cycling {X}{2}{W} ({X}{2}{W}, Discard this card: Draw a card.)"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
