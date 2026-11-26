package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "115")
public class BloodstoneGoblin extends Card {

    public BloodstoneGoblin() {
        // Whenever you cast a spell, if that spell was kicked,
        // Bloodstone Goblin gets +1/+1 and gains menace until end of turn.
        addTrigger(Triggers.youCastSpell().kicked(),
                BoostEffect.self(1, 1),
                new GrantKeywordEffect(Keyword.MENACE, Subjects.source()));
    }
}
