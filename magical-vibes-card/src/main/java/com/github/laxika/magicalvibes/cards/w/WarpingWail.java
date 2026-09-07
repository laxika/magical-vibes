package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "12")
public class WarpingWail extends Card {

    public WarpingWail() {
        var smallCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentPowerAtMostPredicate(1),
                                new PermanentToughnessAtMostPredicate(1))))),
                "Target must be a creature with power or toughness 1 or less.");
        var sorcerySpell = new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.SORCERY_SPELL)),
                "Target must be a sorcery spell.");
        var eldraziScion = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Eldrazi Scion",
                1,
                1,
                null,
                Set.of(),
                List.of(CardSubtype.ELDRAZI, CardSubtype.SCION),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                        "Sacrifice this token: Add {C}.")),
                false,
                false,
                false,
                0,
                Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature with power or toughness 1 or less",
                        new ExileTargetPermanentEffect(),
                        smallCreature),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target sorcery spell",
                        new CounterSpellEffect(),
                        sorcerySpell),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 colorless Eldrazi Scion creature token",
                        eldraziScion))));
    }
}
