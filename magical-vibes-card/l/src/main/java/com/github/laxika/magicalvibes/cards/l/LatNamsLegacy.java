package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ALL", collectorNumber = "30a")
@CardRegistration(set = "ALL", collectorNumber = "30b")
@CardRegistration(set = "CST", collectorNumber = "13")
@CardRegistration(set = "CST", collectorNumber = "30b")
@CardRegistration(set = "ME2", collectorNumber = "52")
public class LatNamsLegacy extends Card {

    public LatNamsLegacy() {
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.cardsInYourHand(null),
                "a card from your hand to shuffle into your library"));
        addSpellEffect(PutCardsIntoLibraryEffect.shuffledIn(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new ConditionalEffect(new HappenedThisWay(StepKind.SHUFFLE_INTO_LIBRARY),
                new RegisterDrawCardsAtNextUpkeepEffect(2)));
    }
}
