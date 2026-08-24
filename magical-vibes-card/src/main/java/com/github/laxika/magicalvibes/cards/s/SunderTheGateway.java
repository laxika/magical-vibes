package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TransformChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "39")
public class SunderTheGateway extends Card {

    public SunderTheGateway() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target nontoken artifact or enchantment an opponent controls. Incubate 2",
                        List.of(destroyTarget(), incubate(2)),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentIsEnchantmentPredicate())))),
                                "Target must be a nontoken artifact or enchantment an opponent controls.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Incubate 2, then transform an Incubator token you control",
                        List.of(incubate(2), new TransformChosenPermanentEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentNamedPredicate("Incubator"),
                                new PermanentControlledBySourceControllerPredicate())))))
        )));
    }

    private static DestroyTargetPermanentEffect destroyTarget() {
        return new DestroyTargetPermanentEffect();
    }

    private static CreateTokenEffect incubate(int counters) {
        ActivatedAbility transform = new ActivatedAbility(
                false,
                "{2}",
                List.of(new TransformSelfEffect()),
                "{2}: Transform this token."
        );
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Incubator", 0, 0, null, null,
                List.of(), Set.of(), Set.of(), false, false, Map.of(), List.of(transform),
                false, false, false, counters, Set.of()
        );
    }
}
