package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "DSC", collectorNumber = "349")
public class NoSecretIsHiddenFromMe extends Card {

    public NoSecretIsHiddenFromMe() {
        CardEffect exile = ExileCardsFromTopUntilEffect.untilNonland(Subjects.you());
        CardEffect cast = new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(StepKind.EXILE));
        addSpellEffect(exile);
        addSpellEffect(cast);
        addSpellEffect(new ConditionalEffect(new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                SequenceEffect.of(exile, cast)));
    }
}
