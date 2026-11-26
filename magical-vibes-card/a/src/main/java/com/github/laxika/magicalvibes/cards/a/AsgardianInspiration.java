package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "678")
public class AsgardianInspiration extends Card {

    public AsgardianInspiration() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(new TriggerCondition(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                UnlessPaysEffect.youMayPay("{2}", new PutCardsIntoHandEffect(Subjects.sourceCard()))
                        .withPrompt("Pay {2} to return Asgardian Inspiration from your graveyard to your hand?"));
    }
}
