package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "40")
@CardRegistration(set = "TSR", collectorNumber = "75")
public class MagusOfTheFuture extends Card {

    public MagusOfTheFuture() {
        addStaticEffect(new PlayWithTopCardRevealedEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(Set.of(
                CardType.CREATURE,
                CardType.ENCHANTMENT,
                CardType.SORCERY,
                CardType.INSTANT,
                CardType.ARTIFACT,
                CardType.PLANESWALKER,
                CardType.BATTLE,
                CardType.KINDRED
        )));
    }
}
