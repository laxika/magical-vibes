package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

public class IsiluCarrierOfTwilight extends Card {

    public IsiluCarrierOfTwilight() {
        // Each other nontoken creature you control has persist.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.PERSIST,
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                ))));

        // At the beginning of your first main phase, you may pay {W}. If you do, transform Isilu.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{W}", new TransformSelfEffect(),
                        "Pay {W} to transform Isilu?"));
    }
}
