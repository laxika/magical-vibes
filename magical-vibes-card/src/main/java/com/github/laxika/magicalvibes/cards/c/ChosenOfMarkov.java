package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MarkovsServant;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "55")
public class ChosenOfMarkov extends Card {

    public ChosenOfMarkov() {
        // Set up back face
        MarkovsServant backFace = new MarkovsServant();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {T}, Tap an untapped Vampire you control: Transform Chosen of Markov.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)),
                        new TransformSelfEffect()),
                "{T}, Tap an untapped Vampire you control: Transform Chosen of Markov."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "MarkovsServant";
    }
}
