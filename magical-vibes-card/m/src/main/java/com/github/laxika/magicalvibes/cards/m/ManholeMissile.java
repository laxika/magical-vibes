package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TMT", collectorNumber = "94")
public class ManholeMissile extends Card {

    public ManholeMissile() {
        addSpellEffect(DealDamageEffect.toTargetCreature(3));
        addSpellEffect(ChooseCardEffect.upTo(Subjects.cardsInYourHand(null), 1,
                "a card from your hand to put on the bottom of your library"));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new ConditionalEffect(new HappenedThisWay(StepKind.PUT_INTO_LIBRARY), new DrawCardEffect(1)));
    }
}
