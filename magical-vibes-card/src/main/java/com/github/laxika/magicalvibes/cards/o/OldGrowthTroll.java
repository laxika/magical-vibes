package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAsAuraEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "185")
public class OldGrowthTroll extends Card {

    public OldGrowthTroll() {
        ControlledPermanentPredicateTargetFilter forestYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                "Target must be a Forest you control");

        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentIsCreaturePredicate(),
                new ReturnSourceAsAuraEffect(forestYouControl)));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null,
                        List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                        "{T}: Add {G}{G}."),
                GrantScope.ENCHANTED_PERMANENT));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, "{1}",
                        List.of(
                                new SacrificeSelfCost(),
                                new CreateTokenEffect(1, "Troll Warrior", 4, 4, CardColor.GREEN,
                                        List.of(CardSubtype.TROLL, CardSubtype.WARRIOR),
                                        Set.of(Keyword.TRAMPLE), Set.of(), true)),
                        "{1}, {T}, Sacrifice this land: Create a tapped 4/4 green Troll Warrior creature token with trample."),
                GrantScope.ENCHANTED_PERMANENT));
    }
}
