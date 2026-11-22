package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "59")
public class StillnessInMotion extends Card {

    public StillnessInMotion() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new MillEffect(3, Subjects.you()),
                new ConditionalEffect(
                        new NotCondition(new CardsInLibraryAtLeast(1)),
                        SequenceEffect.of(
                                new ExileTargetPermanentEffect(Subjects.source()),
                                new ChooseCardEffect(Subjects.cardsInYourGraveyard(null),
                                        "five cards from your graveyard to put on top of your library",
                                        true, new Fixed(5), false, List.of()),
                                PutCardsIntoLibraryEffect.onTop(Subjects.cardsThisWay(StepKind.CHOOSE))
                        ))));
    }
}
