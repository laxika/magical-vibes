package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMD", collectorNumber = "240")
public class ZedruuTheGreathearted extends Card {

    public ZedruuTheGreathearted() {
        PermanentCount permanentsYouOwnOpponentsControl = new PermanentCount(
                new PermanentOwnedBySourceControllerPredicate(), CountScope.OPPONENTS);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new GainLifeEffect(permanentsYouOwnOpponentsControl),
                new DrawCardEffect(permanentsYouOwnOpponentsControl)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{R}{W}",
                List.of(new TargetPlayerGainsControlOfTargetPermanentEffect()),
                "{U}{R}{W}: Target opponent gains control of target permanent you control.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        TargetFilters.permanentYouControl()
                ),
                2,
                2
        ));
    }
}
