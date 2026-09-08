package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "67")
public class SavageBeating extends Card {

    public SavageBeating() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.YOUR_COMBAT);
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{1}{R}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain double strike until end of turn",
                        new GrantKeywordEffect(Set.of(Keyword.DOUBLE_STRIKE), GrantScope.OWN_CREATURES)),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap all creatures you control. After this phase, there is an additional combat phase",
                        List.of(
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                        new PermanentIsCreaturePredicate()),
                                new AdditionalCombatPhaseEffect(1))
                )
        )));
    }
}
