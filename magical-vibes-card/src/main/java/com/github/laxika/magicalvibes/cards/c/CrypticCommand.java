package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "56")
public class CrypticCommand extends Card {

    public CrypticCommand() {
        // Choose two. Each chosen mode's effect declares its own targeting (spell / permanent / none),
        // so the choose-two unwrap needs no per-mode target filters. The {counter, return} pair
        // resolves like Lost in the Mist (spell target + permanent target).
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell",
                        new CounterSpellEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target permanent to its owner's hand",
                        ReturnToHandEffect.target()),
                new ChooseOneEffect.ChooseOneOption(
                        "Tap all creatures your opponents control",
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw a card",
                        new DrawCardEffect())
        ), 2));
    }
}
