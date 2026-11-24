package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellExile;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "143")
@CardRegistration(set = "ECL", collectorNumber = "316")
public class GoliathDaydreamer extends Card {

    public GoliathDaydreamer() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.HAND)),
                new ExileTriggeringSpellEffect(TriggeringSpellExile.DREAM_COUNTER, true));
        addTrigger(Triggers.attacks(),
                ChooseCardEffect.oneOf(Subjects.exiledCards(Subjects.you(), new CardAllOfPredicate(List.of(
                                new CardHasCounterInExilePredicate(CounterType.DREAM),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))))),
                        "a card you own in exile with a dream counter on it"),
                new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
