package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "119")
public class EzuriRenegadeLeader extends Card {

    public EzuriRenegadeLeader() {
        // {G}: Regenerate another target Elf.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new RegenerateEffect(true)),
                "{G}: Regenerate another target Elf.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another Elf"
                )
        ));

        // {2}{G}{G}{G}: Elf creatures you control get +3/+3 and gain trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}{G}",
                List.of(
                        new BoostAllOwnCreaturesEffect(3, 3, new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.ELF))
                ),
                "{2}{G}{G}{G}: Elf creatures you control get +3/+3 and gain trample until end of turn."
        ));
    }
}
