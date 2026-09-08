package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "329")
public class WirewoodLodge extends Card {

    public WirewoodLodge() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {G}, {T}: Untap target Elf.
        PermanentHasSubtypePredicate elf = new PermanentHasSubtypePredicate(CardSubtype.ELF);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, elf)),
                "{G}, {T}: Untap target Elf.",
                new PermanentPredicateTargetFilter(elf, "Target must be an Elf")
        ));
    }
}
