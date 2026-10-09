package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraOrEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "14")
@CardRegistration(set = "PIP", collectorNumber = "366")
@CardRegistration(set = "PIP", collectorNumber = "542")
@CardRegistration(set = "PIP", collectorNumber = "894")
public class CodsworthHandyHelper extends Card {

    public CodsworthHandyHelper() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(2),
                GrantScope.OWN_PERMANENTS,
                new PermanentIsCommanderPredicate()));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(2), GrantScope.SELF,
                new PermanentIsCommanderPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.WHITE,
                        2,
                        ManaRestriction.SubtypeOrPlaneswalkerSpells.auraOrEquipmentSpells())),
                "{T}: Add {W}{W}. Spend this mana only to cast Aura and/or Equipment spells."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AttachTargetAuraOrEquipmentToTargetCreatureEffect()),
                "{T}: Attach target Aura or Equipment you control to target creature you control. "
                        + "Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED,
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.AURA),
                                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))),
                                "Target must be an Aura or Equipment you control"),
                        TargetFilters.creatureYouControl()),
                2,
                2
        ));
    }
}
