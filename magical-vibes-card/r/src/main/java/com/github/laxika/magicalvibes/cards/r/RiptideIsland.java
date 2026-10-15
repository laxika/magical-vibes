package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "64")
public class RiptideIsland extends Card {

    public RiptideIsland() {
        CreateTokenEffect slivers = new CreateTokenEffect(
                2, "Sliver", 1, 1, null,
                List.of(CardSubtype.SLIVER), Set.of(), Set.of());
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, slivers);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, slivers);

        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        PermanentCount sliverCount = new PermanentCount(sliver, CountScope.CONTROLLER);
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES, sliver),
                new BoostAllOwnCreaturesEffect(sliverCount, sliverCount, sliver)));
    }
}
