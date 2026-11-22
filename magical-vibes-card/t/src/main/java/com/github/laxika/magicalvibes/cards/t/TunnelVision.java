package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

@CardRegistration(set = "RAV", collectorNumber = "72")
public class TunnelVision extends Card {

    public TunnelVision() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addSpellEffect(new ChooseCardNameAtResolutionEffect());
        addSpellEffect(new RevealCardsFromTopUntilEffect(Subjects.targetPlayer(), named));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, named), SequenceEffect.of(
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(named)),
                PutCardsIntoLibraryEffect.onTop(Subjects.revealedThisWay(named)))));
        addSpellEffect(new ConditionalEffect(new NotCondition(HappenedThisWay.toACard(StepKind.REVEAL, named)),
                new ShuffleLibraryEffect(true)));
    }
}
