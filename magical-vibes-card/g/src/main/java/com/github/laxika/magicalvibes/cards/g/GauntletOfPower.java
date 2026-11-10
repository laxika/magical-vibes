package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "255")
@CardRegistration(set = "MPS", collectorNumber = "12")
@CardRegistration(set = "TSR", collectorNumber = "267")
@CardRegistration(set = "DMR", collectorNumber = "223")
public class GauntletOfPower extends Card {

    public GauntletOfPower() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSourceChosenColorPredicate()));
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                AddExtraManaOfChosenColorOnLandTapEffect.ofItsController());
    }
}
