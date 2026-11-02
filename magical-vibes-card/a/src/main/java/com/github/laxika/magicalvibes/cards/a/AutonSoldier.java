package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "36")
@CardRegistration(set = "WHO", collectorNumber = "353")
@CardRegistration(set = "WHO", collectorNumber = "641")
@CardRegistration(set = "WHO", collectorNumber = "944")
public class AutonSoldier extends Card {

    public AutonSoldier() {
        addStaticEffect(new CopyPermanentOnEnterEffect(
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
                List.of(new TriggeredAbility(Triggers.attacks(),
                        CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad())),
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
