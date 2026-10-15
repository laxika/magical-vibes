package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlThenCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "577")
public class DalekIntensiveCare extends Card {

    public DalekIntensiveCare() {
        CreateTokenEffect dalekToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Dalek", 3, 3, CardColor.BLACK, null,
                List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT),
                false, false, Map.of(), List.of(), false, false, false, 0,
                Set.of(Keyword.HASTE));
        var nonDalekCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DALEK))));
        var intensiveCare = new ExilePermanentYouControlThenCreateTokenEffect(
                nonDalekCreature, dalekToken);

        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, intensiveCare);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, intensiveCare);

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.DALEK))),
                "Target must be a Dalek you control"));
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new TargetCreatureDealsPowerDamageToAnyTargetEffect(0, 1, false));
    }
}
