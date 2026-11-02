package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "22")
@CardRegistration(set = "MMA", collectorNumber = "26")
@CardRegistration(set = "UMA", collectorNumber = "31")
@CardRegistration(set = "2X2", collectorNumber = "26")
@CardRegistration(set = "C20", collectorNumber = "97")
@CardRegistration(set = "C16", collectorNumber = "74")
@CardRegistration(set = "CM2", collectorNumber = "35")
public class Reveillark extends Card {

    public Reveillark() {
        // Flying is auto-loaded from Scryfall keywords.

        // "When this creature leaves the battlefield, return up to two target creature cards with
        // power 2 or less from your graveyard to the battlefield." The targets are chosen when the
        // trigger is put on the stack.
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtMostPredicate(2))),
                        2, false, false));

        // Evoke {5}{W}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{W}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());
    }
}
