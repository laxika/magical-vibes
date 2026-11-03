package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "203")
public class SandstalkerMoloch extends Card {

    public SandstalkerMoloch() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new OpponentCastSpellThisTurn(new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.BLUE),
                                new CardColorPredicate(CardColor.BLACK)
                        )))),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, new CardIsPermanentPredicate()));
    }
}
