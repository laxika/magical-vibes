package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndCreateTokensForMilledCreaturesEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "161")
public class NecronMonolith extends Card {

    public NecronMonolith() {
        addEffect(EffectSlot.ON_ATTACK,
                new MillControllerAndCreateTokensForMilledCreaturesEffect(
                        3,
                        new CreateTokenEffect(1, "Necron Warrior", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.NECRON, CardSubtype.WARRIOR), Set.of(),
                                Set.of(CardType.ARTIFACT))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(4), AnimatePermanentsEffect.crew()),
                "Crew 4"
        ));
    }
}
