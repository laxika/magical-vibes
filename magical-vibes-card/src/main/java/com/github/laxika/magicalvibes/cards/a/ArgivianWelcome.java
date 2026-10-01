package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBRO", collectorNumber = "16")
public class ArgivianWelcome extends Card {

    public ArgivianWelcome() {
        var largeCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));
        target(new PermanentPredicateTargetFilter(
                largeCreature, "Target must be a creature with power 4 or greater"), 0, 1)
                .addEffect(EffectSlot.SPELL, new DestroyUpToOneTargetPermanentEffect(largeCreature));
        addEffect(EffectSlot.SPELL, new ChooseCardFromHandAndApplyPerpetualKeywordEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), Set.of(Keyword.FLASH)));
    }
}
