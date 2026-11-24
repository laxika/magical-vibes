package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "77")
public class IllusionaryTerrain extends Card {

    public IllusionaryTerrain() {
        // Cumulative upkeep {2}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // As this enchantment enters, choose two basic land types.
        addStaticEffect(new ChooseBasicLandTypeOnEnterEffect(2));

        // Basic lands of the first chosen type are the second chosen type.
        addStaticEffect(GrantSubtypeEffect.secondChosenSubtype(Subjects.allLands(), true,
                new PermanentAllOfPredicate(List.of(new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                        new PermanentHasSourceChosenSubtypePredicate()))));
    }
}
