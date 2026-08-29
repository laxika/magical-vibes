package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "302")
public class KyrenArchive extends Card {

    public KyrenArchive() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExileTopCardsToSourceEffect(1, true),
                "Exile the top card of your library face down?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new DiscardHandCost(),
                        new SacrificeSelfCost(),
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()),
                "{5}, Discard your hand, Sacrifice this artifact: Put all cards exiled with this artifact into their owner's hand."
        ));
    }
}
