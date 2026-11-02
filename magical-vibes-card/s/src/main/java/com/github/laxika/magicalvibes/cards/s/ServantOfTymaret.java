package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "82")
public class ServantOfTymaret extends Card {

    public ServantOfTymaret() {
        addTrigger(Triggers.becomesUntapped(),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true));
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Servant of Tymaret."));
    }
}
