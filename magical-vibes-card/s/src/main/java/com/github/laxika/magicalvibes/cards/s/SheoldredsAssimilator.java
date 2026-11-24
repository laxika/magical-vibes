package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "10")
public class SheoldredsAssimilator extends Card {

    public SheoldredsAssimilator() {
        CardEffect graveyardTrigger = graveyardTrigger();
        var graveyardTarget = target(
                new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 1);
        graveyardTarget.addTrigger(Triggers.enters(ObjectRelation.THIS), graveyardTrigger);
        graveyardTarget.addTrigger(Triggers.attacks(), graveyardTrigger);
    }

    private CardEffect graveyardTrigger() {
        return SequenceEffect.of(
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard(),
                new ConditionalEffect(HappenedThisWay.exiled(),
                        new MayEffect(ConjureCardsEffect.duplicateOf(Subjects.cardsThisWay(StepKind.EXILE),
                                ConjureDestination.intoTopCardsOfLibraryAtRandom(5)),
                                "Conjure a duplicate of that card into the top five cards of your library?")));
    }
}
