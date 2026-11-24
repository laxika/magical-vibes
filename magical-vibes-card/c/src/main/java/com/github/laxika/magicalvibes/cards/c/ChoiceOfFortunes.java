package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "YSNC", collectorNumber = "5")
public class ChoiceOfFortunes extends Card {

    public ChoiceOfFortunes() {
        // "Seek two cards. You may shuffle them into your library. If you do, seek two cards."
        addSpellEffect(new SeekLibraryEffect(2, null));
        addSpellEffect(new ConditionalEffect(new HappenedThisWay(StepKind.SEEK), new MayEffect(SequenceEffect.of(
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.cardsThisWay(StepKind.SEEK)),
                new ShuffleLibraryEffect(false),
                new ConditionalEffect(new HappenedThisWay(StepKind.PUT_INTO_LIBRARY), new SeekLibraryEffect(2, null))),
                "Shuffle them into your library?")));
        addSpellEffect(new GrantNoMaximumHandSizeEffect(Duration.PERMANENT));
    }
}
