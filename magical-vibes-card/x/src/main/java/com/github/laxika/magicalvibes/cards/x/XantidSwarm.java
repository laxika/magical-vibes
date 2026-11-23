package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "135")
@CardRegistration(set = "EMA", collectorNumber = "193")
public class XantidSwarm extends Card {

    public XantidSwarm() {
        addTrigger(Triggers.attacks(), PlayerRestrictionEffect.thisTurn(Subjects.defendingPlayer(),
                PlayerRestriction.castingSpells()));
    }
}
