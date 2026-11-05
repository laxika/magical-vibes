package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "123")
public class Bookwurm extends Card {

    public Bookwurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3), new DrawCardEffect());

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2)),
                "{2}{G}: Put this card from your graveyard into your library third from the top."
        ));
    }
}
