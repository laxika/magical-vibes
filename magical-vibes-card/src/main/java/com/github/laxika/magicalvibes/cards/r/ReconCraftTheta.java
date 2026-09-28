package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "141")
@CardRegistration(set = "PIP", collectorNumber = "436")
@CardRegistration(set = "PIP", collectorNumber = "669")
@CardRegistration(set = "PIP", collectorNumber = "964")
public class ReconCraftTheta extends Card {

    public ReconCraftTheta() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Alien", 0, 0, CardColor.BLUE,
                        Set.of(), List.of(CardSubtype.ALIEN), 1));

        addEffect(EffectSlot.ON_ATTACK, new ProliferateEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
