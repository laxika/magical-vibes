package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "124")
public class NurturerInitiate extends Card {

    public NurturerInitiate() {
        // Whenever a player casts a green spell, you may pay {1}. If you do, target creature gets +1/+1 until end of turn.
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                        new SpellCastTriggerEffect(
                                new CardColorPredicate(CardColor.GREEN),
                                List.of(new BoostTargetCreatureEffect(1, 1)),
                                "{1}"),
                        "Pay {1} to give target creature +1/+1 until end of turn?"));
    }
}
