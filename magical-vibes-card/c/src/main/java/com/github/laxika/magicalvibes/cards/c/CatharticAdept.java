package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ALA", collectorNumber = "34")
public class CatharticAdept extends Card {

    public CatharticAdept() {
        // {T}: Target player mills a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(1, Subjects.targetPlayer())),
                "{T}: Target player mills a card."
        ));
    }
}
