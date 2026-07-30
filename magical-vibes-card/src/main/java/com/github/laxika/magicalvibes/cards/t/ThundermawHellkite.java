package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "150")
public class ThundermawHellkite extends Card {

    public ThundermawHellkite() {
        final PermanentPredicate opponentFliers = new PermanentAllOfPredicate(List.of(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(1, false, false, opponentFliers));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, opponentFliers));
    }
}
