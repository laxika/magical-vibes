package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "13")
public class WallOfStolenIdentity extends Card {

    public WallOfStolenIdentity() {
        // You may have this creature enter as a copy of any creature on the battlefield, except it's
        // a Wall in addition to its other types and has defender. When you do, tap the copied
        // creature and it doesn't untap for as long as you control this creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(),
                "creature",
                null,
                null,
                Set.of(),
                List.of(),
                null,
                null,
                false,
                null,
                Set.of(CardSubtype.WALL),
                Map.of(EffectSlot.ON_ENTER_BATTLEFIELD, List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        DoesntUntapEffect.targetWhileSourceOnBattlefield())),
                false,
                false,
                null,
                Set.of(),
                Set.of(Keyword.DEFENDER),
                false,
                true,
                Set.of(),
                false,
                false,
                null,
                false,
                false
        ));
    }
}
