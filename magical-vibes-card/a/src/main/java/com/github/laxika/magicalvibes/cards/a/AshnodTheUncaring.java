package com.github.laxika.magicalvibes.cards.a;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRC", collectorNumber = "4")
@CardRegistration(set = "BRC", collectorNumber = "47")
public class AshnodTheUncaring extends Card {

    public AshnodTheUncaring() {
        // Whenever you activate an ability of an artifact or creature that isn't a mana ability,
        // if one or more permanents were sacrificed to activate it, you may copy that ability.
        // You may choose new targets for the copy.
        addTrigger(Triggers.youActivateNonManaAbility()
                        .withSource(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())))
                        .withPayment(AbilityTrigger.ActivationPayment.SACRIFICE),
                new MayEffect(new CopyTriggeringSpellOrAbilityEffect(), "Copy that ability?"));
    }
}
