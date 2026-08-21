package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "196")
public class JeskaiRevelation extends Card {

    public JeskaiRevelation() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL,
                new ReturnTargetSpellOrPermanentToHandEffect());

        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target")).addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetEffect(4));

        Map<EffectSlot, CardEffect> monkTokenEffects = Map.of(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 2, "Monk", 1, 1,
                CardColor.WHITE, null, List.of(CardSubtype.MONK),
                Set.of(), Set.of(), false, false, monkTokenEffects, List.of(),
                false, false, false, 0, Set.of()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
