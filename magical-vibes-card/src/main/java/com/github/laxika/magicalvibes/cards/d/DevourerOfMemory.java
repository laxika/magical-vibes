package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "213")
public class DevourerOfMemory extends Card {

    public DevourerOfMemory() {
        addEffect(EffectSlot.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY, SequenceEffect.of(
                new BoostSelfEffect(1, 1),
                new MakeCreatureUnblockableEffect(true)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{B}",
                List.of(new MillEffect(1, MillRecipient.CONTROLLER)),
                "{1}{U}{B}: Mill a card."));
    }
}
