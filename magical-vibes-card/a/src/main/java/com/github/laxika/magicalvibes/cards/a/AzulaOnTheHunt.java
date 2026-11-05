package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "85")
public class AzulaOnTheHunt extends Card {

    public AzulaOnTheHunt() {
        addTrigger(Triggers.attacks(), new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 2));
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new LoseLifeEffect(1),
                CreateTokenEffect.ofClueToken(1).withInvestigate(false)));
    }
}
