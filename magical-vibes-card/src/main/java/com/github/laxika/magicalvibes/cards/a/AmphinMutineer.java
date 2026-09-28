package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "143")
@CardRegistration(set = "LCC", collectorNumber = "143")
public class AmphinMutineer extends Card {

    public AmphinMutineer() {
        PermanentPredicate nonSalamanderCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SALAMANDER))));
        CreateTokenEffect salamanderWarrior = new CreateTokenEffect(
                "Salamander Warrior", 4, 3, CardColor.BLUE,
                List.of(CardSubtype.SALAMANDER, CardSubtype.WARRIOR), Set.of(), Set.of());

        target(new PermanentPredicateTargetFilter(
                        nonSalamanderCreature, "Target must be a non-Salamander creature"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentEffect(salamanderWarrior, nonSalamanderCreature));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{U}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {4}{U}{U}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
