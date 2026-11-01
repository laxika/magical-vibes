package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSacrificedPermanentWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.RollD10Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "21")
public class BagOfDevouring extends Card {

    public BagOfDevouring() {
        PermanentAnyOfPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PermanentAllOfPredicate anotherNontokenArtifactOrCreature = new PermanentAllOfPredicate(List.of(
                artifactOrCreature,
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

        addTrigger(Triggers.sacrificed(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(anotherNontokenArtifactOrCreature,
                        new ExileSacrificedPermanentWithSourceEffect()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(artifactOrCreature, "another artifact or creature"),
                        new DrawCardEffect(1)),
                "{2}, {T}, Sacrifice another artifact or creature: Draw a card."));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new RollD10Effect(new ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect(
                                new EventValue()))),
                "{3}, {T}, Sacrifice this artifact: Roll a d10. Return up to X cards from among cards "
                        + "exiled with this artifact to their owners' hands, where X is the result."));
    }
}
