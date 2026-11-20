package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "165")
@CardRegistration(set = "SPM", collectorNumber = "278")
@CardRegistration(set = "OM1", collectorNumber = "172")
public class InterdimensionalWebWatch extends Card {

    public InterdimensionalWebWatch() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(2, false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.EXILED_SPELL_ONLY)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast spells from exile."
        ));
    }
}
