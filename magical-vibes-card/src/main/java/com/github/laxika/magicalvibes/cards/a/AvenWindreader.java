package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;

import java.util.List;

public class AvenWindreader extends Card {

    public AvenWindreader() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(new RevealTopCardOfLibraryEffect()), true, "{1}{U}: Target player reveals the top card of their library."));
    }
}
