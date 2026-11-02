package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "240")
@CardRegistration(set = "SLD", collectorNumber = "663")
@CardRegistration(set = "TSR", collectorNumber = "254")
public class HarmonicSliver extends Card {

    public HarmonicSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        PermanentAnyOfPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()
        ));
        DestroyTargetPermanentEffect destroyArtifactOrEnchantment =
                new DestroyTargetPermanentEffect(artifactOrEnchantment);

        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                destroyArtifactOrEnchantment),
                GrantScope.ALL_CREATURES,
                sliver));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                destroyArtifactOrEnchantment),
                GrantScope.SELF,
                sliver));
    }
}
