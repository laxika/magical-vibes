package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "67")
public class GrizzledAngler extends Card {

    public GrizzledAngler() {
        GrislyAnglerfish backFace = new GrislyAnglerfish();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        var colorlessCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardIsColorlessPredicate()));

        // {T}: Mill two cards. Then if there is a colorless creature card in your graveyard,
        // transform this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new ConditionalEffect(
                                new GraveyardCardThreshold(1, colorlessCreature),
                                new TransformSelfEffect())),
                "{T}: Mill two cards. Then if there is a colorless creature card in your graveyard, "
                        + "transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GrislyAnglerfish";
    }
}
