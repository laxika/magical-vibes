package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "33")
@CardRegistration(set = "MIC", collectorNumber = "71")
public class VisionsOfDuplicity extends Card {

    public VisionsOfDuplicity() {
        var creatureNotControlledByCaster = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        target(new PermanentPredicateTargetFilter(
                creatureNotControlledByCaster, "Target must be a creature you don't control"));
        target(new PermanentPredicateTargetFilter(
                creatureNotControlledByCaster, "Target must be a creature you don't control"))
                .addEffect(EffectSlot.SPELL, new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentIsCreaturePredicate(), false));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new ReduceOwnCastCostEffect(new GreatestManaValueAmongOwnedCommanders())));
        addCastingOption(new FlashbackCast("{8}{U}{U}"));
    }
}
