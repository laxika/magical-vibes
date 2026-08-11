package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "201")
public class VinebredBrawler extends Card {

    public VinebredBrawler() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());

        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(targetFilter, "Target must be another Elf you control"))
                .addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(2, 1, targetFilter));
    }
}
