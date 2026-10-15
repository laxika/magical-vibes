package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "59")
public class IronLadDivergingDestiny extends Card {

    public IronLadDivergingDestiny() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                        new ConditionalEffect(
                                new TopCardOfLibraryType(CardType.ARTIFACT),
                                new DrawCardEffect(1))),
                "{T}: Reveal the top card of your library. If it's an artifact card, draw a card."));
    }
}
