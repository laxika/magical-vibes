package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "20")
public class IcingdeathFrostTyrant extends Card {

    public IcingdeathFrostTyrant() {
        addEffect(EffectSlot.ON_DEATH, frostTongueToken());
    }

    private static CreateTokenEffect frostTongueToken() {
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Icingdeath, Frost Tongue", 0, 0, CardColor.WHITE, null,
                List.of(CardSubtype.EQUIPMENT), Set.of(), Set.of(), false, false,
                Map.of(
                        EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE),
                        EffectSlot.ON_ATTACK, new TapPermanentsEffect(
                                TapUntapScope.TARGET,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledByDefendingPlayerPredicate())))
                ),
                List.of(new EquipActivatedAbility("{2}")),
                false, false, true, 0, Set.of());
    }
}
