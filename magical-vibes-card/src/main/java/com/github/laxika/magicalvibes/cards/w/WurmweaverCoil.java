package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "99")
public class WurmweaverCoil extends Card {

    public WurmweaverCoil() {
        PermanentAllOfPredicate greenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));

        target(new PermanentPredicateTargetFilter(greenCreature, "Target must be a green creature"))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(6, 6, GrantScope.ENCHANTED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                "Wurm",
                                6,
                                6,
                                CardColor.GREEN,
                                List.of(CardSubtype.WURM),
                                Set.of(),
                                Set.of())),
                "Sacrifice this Aura: Create a 6/6 green Wurm creature token."
        ));
    }
}
