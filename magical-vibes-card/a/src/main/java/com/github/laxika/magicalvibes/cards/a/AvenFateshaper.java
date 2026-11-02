package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "69")
@CardRegistration(set = "DMR", collectorNumber = "40")
public class AvenFateshaper extends Card {

    public AvenFateshaper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReorderTopCardsOfLibraryEffect(4));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new ReorderTopCardsOfLibraryEffect(4)),
                "{4}{U}: Look at the top four cards of your library, then put them back in any order."
        ));
    }
}
