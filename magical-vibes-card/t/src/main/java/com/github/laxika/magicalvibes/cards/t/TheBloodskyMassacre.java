package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "207")
public class TheBloodskyMassacre extends Card {

    public TheBloodskyMassacre() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Demon Berserker", 2, 3, CardColor.RED,
                List.of(CardSubtype.DEMON, CardSubtype.BERSERKER), Set.of(Keyword.MENACE), Set.of()));
        addTrigger(Triggers.chapter(2), new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.creatureAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.BERSERKER),
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))))));
        addTrigger(Triggers.chapter(3), new AwardPersistentManaEffect(
                ManaColor.RED,
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.BERSERKER), CountScope.CONTROLLER)));
    }
}
