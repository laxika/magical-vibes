package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseNameAndNumberRevealLibraryDamageEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "180")
public class Mindblaze extends Card {

    public Mindblaze() {
        addSpellEffect(new ChooseNameAndNumberRevealLibraryDamageEffect(List.of(CardType.LAND), 8));
    }
}
