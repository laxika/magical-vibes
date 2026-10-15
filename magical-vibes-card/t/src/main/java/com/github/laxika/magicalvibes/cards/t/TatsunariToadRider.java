package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "123")
public class TatsunariToadRider extends Card {

    private static final PermanentPredicate KEIMI = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNamedPredicate("Keimi")));
    private static final PermanentPredicate FROG_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.FROG)));
    private static final PermanentPredicate FLYING_OR_REACH = new PermanentAnyOfPredicate(List.of(
            new PermanentHasKeywordPredicate(Keyword.FLYING),
            new PermanentHasKeywordPredicate(Keyword.REACH)));

    public TatsunariToadRider() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                new CardTypePredicate(CardType.ENCHANTMENT),
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 1, "Keimi", 3, 3,
                        CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                        List.of(CardSubtype.FROG), Set.of(), Set.of(), false, false,
                        Map.of(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                                new SpellCastTriggerEffect(
                                        new CardTypePredicate(CardType.ENCHANTMENT),
                                        List.of(
                                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                                                new GainLifeEffect(1)))),
                        List.of(), false, false, true, 0, Set.of())),
                new NotCondition(new ControlsPermanent(KEIMI))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G/U}",
                List.of(
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                FLYING_OR_REACH, "creatures with flying or reach", true),
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                FLYING_OR_REACH, "creatures with flying or reach")),
                "{1}{G/U}: Tatsunari and target Frog you control can't be blocked this turn except by creatures with flying or reach.",
                new ControlledPermanentPredicateTargetFilter(
                        FROG_CREATURE, "Target must be a Frog you control")));
    }
}
