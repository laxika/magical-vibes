package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "56")
public class GoblinTrapfinder extends Card {

    public GoblinTrapfinder() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                SequenceEffect.of(
                        new SeekLibraryEffect(1, new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE), new CardMaxManaValuePredicate(3)))),
                        PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(
                                Subjects.cardsThisWay(StepKind.SEEK), Keyword.HASTE),
                        new PerpetuallyChangeCastCostEffect(Subjects.cardsThisWay(StepKind.SEEK), -1),
                        new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.SEEK),
                                Triggers.endStep(ActivePlayer.YOU), new SacrificeSelfEffect())));
    }
}
