package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "5")
public class SurgeOfAcclaim extends Card {

    public SurgeOfAcclaim() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Seek a card with start your engines!",
                        new SeekLibraryToHandEffect(new CardKeywordPredicate(Keyword.START_YOUR_ENGINES))),
                new ChooseOneEffect.ChooseOneOption(
                        "Seek a nonland card",
                        new SeekLibraryToHandEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND))))
        ), new MaxSpeed()));

        addCastingOption(new JumpStartCast());
    }
}
