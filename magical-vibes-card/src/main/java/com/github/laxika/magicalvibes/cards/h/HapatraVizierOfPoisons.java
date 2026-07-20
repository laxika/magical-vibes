package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "199")
public class HapatraVizierOfPoisons extends Card {

    public HapatraVizierOfPoisons() {
        // Whenever Hapatra deals combat damage to a player, you may put a -1/-1 counter on target
        // creature. The target (any creature) is chosen at resolution via the combat-damage may flow.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        "Put a -1/-1 counter on target creature?"
                ));

        // Whenever you put one or more -1/-1 counters on a creature, create a 1/1 green Snake creature
        // token with deathtouch. Once-per-creature cadence (one Snake regardless of how many counters
        // were placed at once), controller-restricted (an opponent's counters do not trigger it).
        addEffect(EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE,
                new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SNAKE), Set.of(Keyword.DEATHTOUCH), Set.of()));
    }
}
