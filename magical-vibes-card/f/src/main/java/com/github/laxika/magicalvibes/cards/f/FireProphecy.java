package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "116")
public class FireProphecy extends Card {

    public FireProphecy() {
        target(TargetFilters.creature()).addSpellEffect(new DealDamageToTargetCreatureEffect(3));
        addSpellEffect(ChooseCardEffect.upTo(Subjects.cardsInYourHand(null), 1,
                "a card from your hand to put on the bottom of your library"));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new ConditionalEffect(new HappenedThisWay(StepKind.PUT_INTO_LIBRARY), new DrawCardEffect(1)));
    }
}
