package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseOrStopEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "90")
public class FieryGambit extends Card {

    public FieryGambit() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "You must target a creature."
        )).addEffect(EffectSlot.SPELL, new FlipUntilLoseOrStopEffect(List.of(
                new DealDamageToTargetCreatureEffect(3),
                new DealDamageToPlayersEffect(6, DamageRecipient.EACH_OPPONENT),
                SequenceEffect.of(
                        new DrawCardEffect(9),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()))
        )));
    }
}
