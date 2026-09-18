package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "222")
public class TatyovaStewardOfTides extends Card {

    public TatyovaStewardOfTides() {
        // Land creatures you control have flying.
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES,
                        new PermanentIsLandPredicate()));

        // Whenever a land you control enters, if you control seven or more lands, up to one target
        // land you control becomes a 3/3 Elemental creature with haste. It's still a land.
        target(TargetFilters.landYouControl(), 0, 1)
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new ConditionalEffect(
                                new ControlsPermanentCount(7, new PermanentIsLandPredicate()),
                                new AnimatePermanentsEffect(
                                        3, 3,
                                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE),
                                        null, Set.of(), GrantScope.TARGET, EffectDuration.PERMANENT)));
    }
}
