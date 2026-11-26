package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "326")
@CardRegistration(set = "TPR", collectorNumber = "244")
@CardRegistration(set = "VMA", collectorNumber = "315")
public class SkyshroudForest extends Card {

    public SkyshroudForest() {
        // This land enters tapped.
        addStaticEffect(new EntersTappedEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {G}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), DealDamageEffect.to(Subjects.you(), 1)),
                "{T}: Add {G}. Skyshroud Forest deals 1 damage to you."
        ));
        // {T}: Add {U}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), DealDamageEffect.to(Subjects.you(), 1)),
                "{T}: Add {U}. Skyshroud Forest deals 1 damage to you."
        ));
    }
}
