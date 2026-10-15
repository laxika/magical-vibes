package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.FullParty;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "226")
public class LinvalaShieldOfSeaGate extends Card {

    public LinvalaShieldOfSeaGate() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                        new FullParty(),
                        new LockTargetPermanentEffect(true, true, true,
                                EffectDuration.UNTIL_YOUR_NEXT_TURN, TargetPredicates.permanent())
                ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Hexproof",
                                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES)),
                                new ChooseOneEffect.ChooseOneOption("Indestructible",
                                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_OWN_CREATURES))
                        ))
                ),
                "Sacrifice Linvala: Choose hexproof or indestructible. Creatures you control gain that ability until end of turn."
        ));
    }
}
