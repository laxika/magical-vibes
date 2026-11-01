package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

public class CasalPathbreakerOwlbear extends Card {

    public CasalPathbreakerOwlbear() {
        PermanentHasSupertypePredicate legendary =
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);
        PermanentAllOfPredicate otherLegendary = new PermanentAllOfPredicate(List.of(
                legendary,
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new BoostAllOwnCreaturesEffect(2, 2, otherLegendary),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, legendary));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new TransformSelfEffect());
    }
}
