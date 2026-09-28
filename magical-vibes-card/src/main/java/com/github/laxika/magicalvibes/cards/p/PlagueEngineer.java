package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1215")
@CardRegistration(set = "MH1", collectorNumber = "100")
public class PlagueEngineer extends Card {

    public PlagueEngineer() {
        // As this creature enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Creatures of the chosen type your opponents control get -1/-1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OPPONENT_CREATURES,
                new PermanentHasSourceChosenSubtypePredicate()));
    }
}
