package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "90")
public class YukoraThePrisoner extends Card {

    public YukoraThePrisoner() {
        // When Yukora leaves the battlefield, sacrifice all non-Ogre creatures you control.
        // The count equals the number of matching creatures, so the handler sacrifices every one
        // of them with no choice prompt. Yukora is already gone when this resolves, so it is never
        // counted itself.
        PermanentPredicate nonOgreCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.OGRE))));

        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new SacrificePermanentsEffect(
                new PermanentCount(nonOgreCreature, CountScope.CONTROLLER),
                nonOgreCreature,
                SacrificeRecipient.CONTROLLER));
    }
}
