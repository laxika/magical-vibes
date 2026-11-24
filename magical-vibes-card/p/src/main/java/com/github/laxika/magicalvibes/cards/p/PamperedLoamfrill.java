package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "17")
public class PamperedLoamfrill extends Card {

    public PamperedLoamfrill() {
        var anotherCreatureCard = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardIsSelfPredicate())));
        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        ConjureCardsEffect.duplicateOf(
                                Subjects.targetCard(anotherCreatureCard, GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                                ConjureDestination.ontoTopOfLibrary()),
                        PerpetuallyBoostCardEffect.toCards(Subjects.cardsThisWay(StepKind.CONJURE), 1, 1),
                        PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(
                                Subjects.cardsThisWay(StepKind.CONJURE), Keyword.DEATHTOUCH)),
                "Renew {1}{G} ({1}{G}, Exile this card from your graveyard: Conjure a duplicate of another target "
                        + "creature card in your graveyard onto the top of your library. The duplicate perpetually gets "
                        + "+1/+1 and gains deathtouch. Activate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
