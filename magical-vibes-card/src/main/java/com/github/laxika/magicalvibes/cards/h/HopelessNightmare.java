package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "95")
public class HopelessNightmare extends Card {

    public HopelessNightmare() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)
        ));
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new ScryEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeSelfCost()),
                "{2}{B}: Sacrifice this enchantment."
        ));
    }
}
