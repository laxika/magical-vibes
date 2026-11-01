package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "243")
@CardRegistration(set = "LCI", collectorNumber = "311")
public class VitoFanaticOfAclazotz extends Card {

    public VitoFanaticOfAclazotz() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        new SequenceEffect(List.of(
                                new ConditionalEffect(new NthAbilityResolutionThisTurn(1),
                                        new GainLifeEffect(2)),
                                new ConditionalEffect(new NthAbilityResolutionThisTurn(2),
                                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)),
                                new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                                        new CreateTokenEffect(
                                                1, "Vampire Demon", 4, 3, CardColor.WHITE,
                                                Set.of(CardColor.WHITE, CardColor.BLACK),
                                                List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
                                                Set.of(Keyword.FLYING), Set.of()))
                        ))));
    }
}
