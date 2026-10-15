package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "44")
public class EssenceAnchor extends Card {

    public EssenceAnchor() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(
                        "Zombie Druid", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE, CardSubtype.DRUID), Set.of(), Set.of()
                )),
                "{T}: Create a 2/2 black Zombie Druid creature token. Activate only during your turn and only if a card left your graveyard this turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withActivationCondition(
                new CardsLeftGraveyardThisTurn(),
                "Activate only if a card left your graveyard this turn"
        ));
    }
}
