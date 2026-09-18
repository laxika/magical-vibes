package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "116")
public class FoundingOfOmashu extends Card {

    public FoundingOfOmashu() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new CreateTokenEffect(2, "Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ALLY), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new DrawCardEffect(1)),
                "Discard a card?"));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
