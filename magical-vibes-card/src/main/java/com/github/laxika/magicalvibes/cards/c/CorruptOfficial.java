package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "128")
public class CorruptOfficial extends Card {

    public CorruptOfficial() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Corrupt Official."));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER, true));
    }
}
