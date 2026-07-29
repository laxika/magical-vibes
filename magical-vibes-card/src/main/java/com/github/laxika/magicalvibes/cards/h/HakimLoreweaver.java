package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToSourcePredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "68")
public class HakimLoreweaver extends Card {

    public HakimLoreweaver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsAuraPredicate())
                        .targetGraveyard(true)
                        .attachToSource(true)
                        .build()),
                "{U}{U}: Return target Aura card from your graveyard to the battlefield attached to Hakim, "
                        + "Loreweaver. Activate only during your upkeep and only if Hakim isn't enchanted.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withActivationCondition(new NotCondition(new Enchanted()),
                "Activate only if Hakim, Loreweaver isn't enchanted"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(new DestroyAllPermanentsEffect(new PermanentIsAuraAttachedToSourcePredicate())),
                "{U}{U}, {T}: Destroy all Auras attached to Hakim, Loreweaver."
        ));
    }
}
