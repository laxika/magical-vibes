package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "36")
@CardRegistration(set = "WHO", collectorNumber = "353")
public class AutonSoldier extends Card {

    public AutonSoldier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(),
                "creature",
                null,
                null,
                Set.of(CardType.ARTIFACT),
                List.of(),
                null,
                null,
                false,
                null,
                Set.of(),
                Map.of(EffectSlot.ON_ATTACK,
                        List.of(CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad())),
                false,
                false,
                null,
                Set.of(),
                Set.of(),
                false,
                true,
                Set.of(CardSupertype.LEGENDARY),
                false,
                false,
                null,
                false,
                false
        ));
    }
}
