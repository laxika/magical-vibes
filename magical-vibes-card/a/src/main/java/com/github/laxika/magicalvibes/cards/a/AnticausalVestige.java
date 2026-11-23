package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostControlledLandsPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "1")
public class AnticausalVestige extends Card {

    public AnticausalVestige() {
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(1),
                new MayEffect(new PutCardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardManaValueAtMostControlledLandsPredicate())),
                        "permanent", true),
                        "You may put a permanent card with mana value less than or equal to the number of lands you "
                                + "control from your hand onto the battlefield tapped.")));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}"))));
    }
}
