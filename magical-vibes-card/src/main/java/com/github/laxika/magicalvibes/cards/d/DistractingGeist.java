package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CleverDistraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "9")
public class DistractingGeist extends Card {

    private static final PermanentAllOfPredicate DEFENDING_PLAYER_CREATURE =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledByDefendingPlayerPredicate()));

    public DistractingGeist() {
        setBackFaceCard(new CleverDistraction());

        target(new PermanentPredicateTargetFilter(
                DEFENDING_PLAYER_CREATURE,
                "Target must be a creature defending player controls"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new TapPermanentsEffect(TapUntapScope.TARGET, DEFENDING_PLAYER_CREATURE));

        addCastingOption(new DisturbCast("{4}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CleverDistraction";
    }
}
