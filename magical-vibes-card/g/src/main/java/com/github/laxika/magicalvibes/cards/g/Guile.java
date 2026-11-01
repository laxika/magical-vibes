package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControlledCounterWithExileAndPlayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "69")
@CardRegistration(set = "DD2", collectorNumber = "14")
@CardRegistration(set = "MM2", collectorNumber = "46")
@CardRegistration(set = "JVC", collectorNumber = "14")
public class Guile extends Card {

    public Guile() {
        // "This creature can't be blocked except by three or more creatures."
        addStaticEffect(new CantBeBlockedByFewerThanNCreaturesEffect(3));
        // "If a spell or ability you control would counter a spell, instead exile that spell and you
        // may play that card without paying its mana cost."
        addStaticEffect(new ReplaceControlledCounterWithExileAndPlayEffect());
        // "When Guile is put into a graveyard from anywhere, shuffle it into its owner's library."
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS), new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
