package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "89")
public class FaerieDreamthief extends Card {

    public FaerieDreamthief() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));

        // {2}{B}, Exile this card from your graveyard: You draw a card and you lose 1 life.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1)
                ),
                "{2}{B}, Exile this card from your graveyard: You draw a card and you lose 1 life."
        ));
    }
}
