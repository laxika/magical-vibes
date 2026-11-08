package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "153")
public class KurkeshOnakkeAncient extends Card {

    public KurkeshOnakkeAncient() {
        // Whenever you activate an ability of an artifact, if it isn't a mana ability, you may pay
        // {R}. If you do, copy that ability. You may choose new targets for the copy.
        addTrigger(Triggers.youActivateNonManaAbility().withSource(new PermanentIsArtifactPredicate()),
                new MayPayManaEffect("{R}", new CopyTriggeringSpellOrAbilityEffect(),
                        "Pay {R} to copy that ability?"));
    }
}
