package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "89")
@CardRegistration(set = "WOE", collectorNumber = "377")
public class FaerieDreamthief extends Card {

    public FaerieDreamthief() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(1));

        // {2}{B}, Exile this card from your graveyard: You draw a card and you lose 1 life.
        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
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
