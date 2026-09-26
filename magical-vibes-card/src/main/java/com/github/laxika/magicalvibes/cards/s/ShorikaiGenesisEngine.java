package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1880")
public class ShorikaiGenesisEngine extends Card {

    public ShorikaiGenesisEngine() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new CreateTokenEffect(
                                1,
                                "Pilot",
                                1,
                                1,
                                null,
                                List.of(CardSubtype.PILOT),
                                Set.of(),
                                Set.of(),
                                Map.of(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2)))),
                "{1}, {T}: Draw two cards, then discard a card. Create a 1/1 colorless Pilot creature token "
                        + "with \"This token crews Vehicles as though its power were 2 greater.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(8), AnimatePermanentsEffect.crew()),
                "Crew 8"
        ));
    }
}
