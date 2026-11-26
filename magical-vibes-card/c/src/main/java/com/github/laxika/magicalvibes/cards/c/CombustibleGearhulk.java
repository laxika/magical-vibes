package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "112")
@CardRegistration(set = "MPS", collectorNumber = "4")
@CardRegistration(set = "KLR", collectorNumber = "120")
@CardRegistration(set = "FIC", collectorNumber = "292")
@CardRegistration(set = "C21", collectorNumber = "163")
@CardRegistration(set = "DSC", collectorNumber = "163")
@CardRegistration(set = "M3C", collectorNumber = "210")
@CardRegistration(set = "DRC", collectorNumber = "102")
@CardRegistration(set = "SCD", collectorNumber = "128")
public class CombustibleGearhulk extends Card {

    public CombustibleGearhulk() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new DrawCardEffect(3),
                "Have your opponent draw three cards? If not, they mill three cards and this creature deals"
                        + " damage to you equal to the total mana value of those cards.",
                SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        DealDamageEffect.to(Subjects.targetPlayer(), AmountThisWay.milledManaValue())),
                MayChoicePlayer.TARGET_PLAYER));
    }
}
