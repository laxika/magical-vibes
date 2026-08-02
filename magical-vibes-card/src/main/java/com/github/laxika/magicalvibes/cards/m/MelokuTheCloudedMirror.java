package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "74")
public class MelokuTheCloudedMirror extends Card {

    public MelokuTheCloudedMirror() {
        // {1}, Return a land you control to its owner's hand: Create a 1/1 blue Illusion
        // creature token with flying.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new CreateTokenEffect("Illusion", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.ILLUSION), Set.of(Keyword.FLYING), Set.of())),
                "{1}, Return a land you control to its owner's hand: Create a 1/1 blue Illusion creature token with flying."));
    }
}
