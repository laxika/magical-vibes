package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "223")
public class ToriDAvenantFuryRider extends Card {

    public ToriDAvenantFuryRider() {
        var otherAttackingCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 1, otherAttackingCreatures));

        var otherRedAttackingCreatures = new PermanentAllOfPredicate(List.of(
                otherAttackingCreatures,
                new PermanentColorInPredicate(Set.of(CardColor.RED))
        ));
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.OWN_CREATURES, otherRedAttackingCreatures));

        var otherWhiteAttackingCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))
        ));
        addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(
                TapUntapScope.OTHER_CONTROLLED_CREATURES, otherWhiteAttackingCreatures));
    }
}
