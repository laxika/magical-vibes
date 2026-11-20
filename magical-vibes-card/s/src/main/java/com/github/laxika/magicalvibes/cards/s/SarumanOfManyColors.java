package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachOpponentThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostEventValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "35")
@CardRegistration(set = "HOC", collectorNumber = "75")
public class SarumanOfManyColors extends Card {

    public SarumanOfManyColors() {
        CardPredicate noncreatureSpell = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessDiscardsEffect(noncreatureSpell));
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new MillEachOpponentThenIfMilledEffect(
                        2,
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                ExileGraveyardCardsEffect.oneTargetFromOpponentGraveyard(new CardAllOfPredicate(
                                        List.of(noncreatureSpell, new CardManaValueAtMostEventValuePredicate()))),
                                CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE))))));
    }
}
