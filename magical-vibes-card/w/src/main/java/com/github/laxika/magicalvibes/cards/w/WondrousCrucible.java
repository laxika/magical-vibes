package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRC", collectorNumber = "20")
@CardRegistration(set = "BRC", collectorNumber = "67")
public class WondrousCrucible extends Card {

    public WondrousCrucible() {
        addStaticEffect(new GrantKeywordEffect(Keyword.WARD, Subjects.otherPermanentsYouControl()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(2)), Subjects.permanentsYouControl()));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new MillEffect(2, Subjects.you()),
                ExileCardAtRandomEffect.fromGraveyard(Subjects.you(),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
