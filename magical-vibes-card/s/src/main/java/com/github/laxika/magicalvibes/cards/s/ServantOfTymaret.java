package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "82")
public class ServantOfTymaret extends Card {

    public ServantOfTymaret() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true));
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Servant of Tymaret."));
    }
}
