package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "53")
@CardRegistration(set = "ACR", collectorNumber = "143")
public class EdwardKenway extends Card {

    public EdwardKenway() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                CreateTokenEffect.ofTreasureToken(new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTappedPredicate(),
                                new PermanentHasAnySubtypePredicate(Set.of(
                                        CardSubtype.ASSASSIN,
                                        CardSubtype.PIRATE,
                                        CardSubtype.VEHICLE)))),
                        CountScope.CONTROLLER)));

        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VEHICLE)),
                                new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect())));
    }
}
