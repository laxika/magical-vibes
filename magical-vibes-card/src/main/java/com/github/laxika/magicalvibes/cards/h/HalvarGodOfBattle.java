package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SwordOfTheRealms;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraOrEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToCreatureControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "15")
public class HalvarGodOfBattle extends Card {

    public HalvarGodOfBattle() {
        setBackFaceCard(new SwordOfTheRealms());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsEnchantedPredicate(),
                        new PermanentIsEquippedPredicate()))));

        PermanentPredicateTargetFilter attachmentTarget = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAuraAttachedToCreaturePredicate(),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                                        new PermanentAttachedToCreatureControlledBySourceControllerPredicate())))),
                        new PermanentAttachedToCreatureControlledBySourceControllerPredicate())),
                "Target must be an Aura or Equipment attached to a creature you control");
        target(attachmentTarget);
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate())),
                "Target must be a creature you control"));
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                new AttachTargetAuraOrEquipmentToTargetCreatureEffect(),
                "Attach target Aura or Equipment to target creature you control?"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Halvar, God of Battle", List.of())
                        .withManaCost("{2}{W}{W}"),
                new ChooseOneEffect.ChooseOneOption("Sword of the Realms", List.of())
                        .withManaCost("{1}{W}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SwordOfTheRealms";
    }
}
