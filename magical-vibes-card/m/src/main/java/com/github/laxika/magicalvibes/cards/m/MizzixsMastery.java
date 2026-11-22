package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "STA", collectorNumber = "43")
@CardRegistration(set = "FCA", collectorNumber = "41")
@CardRegistration(set = "RVR", collectorNumber = "118")
@CardRegistration(set = "C15", collectorNumber = "29")
@CardRegistration(set = "OTC", collectorNumber = "175")
public class MizzixsMastery extends Card {

    public MizzixsMastery() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{R}{R}{R}"))));
        addSpellEffect(new ConditionalReplacementEffect(
                new Overloaded(),
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(instantOrSorcery),
                        CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE))),
                SequenceEffect.of(
                        new ExileGraveyardCardsEffect(0, GraveyardExileScope.OWN_ALL_MATCHING, instantOrSorcery),
                        CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE)))));
        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, ownGraveyard));
        addSpellEffect(new ExileSpellEffect());
    }
}
