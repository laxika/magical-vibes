package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "203")
public class ErthaJoFrontierMentor extends Card {

    public ErthaJoFrontierMentor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, mercenaryToken());
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(
                        null,
                        null,
                        false,
                        false,
                        new StackEntryAnyOfPredicate(List.of(
                                new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()),
                                new StackEntryTargetsAnyPlayerPredicate()))));
    }

    private static CreateTokenEffect mercenaryToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Mercenary", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.MERCENARY), Set.of(), Set.of(), false, false, Map.of(),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new BoostTargetCreatureEffect(1, 0)),
                        "{T}: Target creature you control gets +1/+0 until end of turn. Activate only as a sorcery.",
                        TargetFilters.creatureYouControl(),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                )),
                false, false, false, 0, Set.of());
    }
}
