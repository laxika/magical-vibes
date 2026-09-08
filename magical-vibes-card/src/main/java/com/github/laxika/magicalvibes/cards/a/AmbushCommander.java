package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "111")
public class AmbushCommander extends Card {

    public AmbushCommander() {
        addEffect(EffectSlot.STATIC,
                new AllLandsAreCreaturesEffect(1, 1, CardSubtype.FOREST, CardColor.GREEN,
                        GrantScope.OWN_LANDS));
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.ELF, GrantScope.OWN_PERMANENTS, false,
                        new PermanentHasSubtypePredicate(CardSubtype.FOREST)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                "an Elf",
                                false),
                        new BoostTargetCreatureEffect(3, 3)),
                "{1}{G}, Sacrifice an Elf: Target creature gets +3/+3 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
