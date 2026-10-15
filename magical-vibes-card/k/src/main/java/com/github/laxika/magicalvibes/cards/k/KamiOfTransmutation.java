package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardTypeToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "3")
public class KamiOfTransmutation extends Card {

    public KamiOfTransmutation() {
        ChooseOneAtTriggerTimeEffect choice = new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each permanent card in your hand perpetually becomes an artifact in addition to its other types.",
                        new PerpetuallyGrantCardTypeToMatchingHandCardsEffect(
                                CardType.ARTIFACT, new CardIsPermanentPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Each permanent card in your hand perpetually becomes an enchantment in addition to its other types.",
                        new PerpetuallyGrantCardTypeToMatchingHandCardsEffect(
                                CardType.ENCHANTMENT, new CardIsPermanentPredicate())))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, choice);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, choice);
    }
}
