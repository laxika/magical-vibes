package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureUnderYourControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "114")
public class LimDLTheNecromancer extends Card {

    public LimDLTheNecromancer() {
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), new MayPayManaEffect(
                "{1}{B}",
                new ReturnDyingOpponentCreatureUnderYourControlEffect(CardSubtype.ZOMBIE),
                "Pay {1}{B} to return that card to the battlefield under your control?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect(true)),
                "{1}{B}: Regenerate target Zombie.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        "Target must be a Zombie"
                )
        ));
    }
}
