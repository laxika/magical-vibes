package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "174")
public class HearthcageGiant extends Card {

    public HearthcageGiant() {
        // When this creature enters, create two 3/1 red Elemental Shaman creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Elemental Shaman", 3, 1, CardColor.RED,
                        List.of(CardSubtype.ELEMENTAL, CardSubtype.SHAMAN), Set.of(), Set.of()));

        // Sacrifice an Elemental: Target Giant creature gets +3/+1 until end of turn.
        // Hearthcage Giant is a Giant Warrior (not an Elemental), so it can only sacrifice the tokens.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL)
                                )),
                                "Sacrifice an Elemental",
                                false
                        ),
                        new BoostTargetCreatureEffect(3, 1)
                ),
                "Sacrifice an Elemental: Target Giant creature gets +3/+1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.GIANT)
                        )),
                        "Target must be a Giant creature"
                )
        ));
    }
}
