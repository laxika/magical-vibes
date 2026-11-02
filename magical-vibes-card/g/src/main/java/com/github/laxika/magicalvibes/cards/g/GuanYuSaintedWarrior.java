package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "6")
@CardRegistration(set = "ME3", collectorNumber = "12")
public class GuanYuSaintedWarrior extends Card {

    public GuanYuSaintedWarrior() {
        // Horsemanship is auto-loaded from Scryfall (evasion handled by the combat engine).
        // "When Guan Yu is put into your graveyard from the battlefield, you may shuffle Guan Yu
        // into your library."
        addTrigger(Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), ObjectRelation.THIS),
                new MayEffect(new ShuffleSelfFromGraveyardIntoLibraryEffect(), "Shuffle Guan Yu into your library?"));
    }
}
