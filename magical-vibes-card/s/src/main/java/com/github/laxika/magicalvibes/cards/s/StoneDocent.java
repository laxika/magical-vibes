package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "36")
public class StoneDocent extends Card {

    public StoneDocent() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new GainLifeEffect(2),
                        new SurveilEffect(1)
                ),
                "{W}, Exile this card from your graveyard: You gain 2 life. Surveil 1. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
