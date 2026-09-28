package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect.CopyCastCost;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "37")
public class SignatureSpells extends Card {

    public SignatureSpells() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SeekLibraryEffect(
                new Fixed(2),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                LibrarySearchDestination.EXILE_WITH_SOURCE,
                new ManaValueBound(new Fixed(3), true, 0)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CopyCardsExiledWithSourceAndMayCastCopiesEffect(false, CopyCastCost.FREE),
                "Copy a card exiled with Signature Spells?"));
    }
}
