package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "118")
public class TymaretCallsTheDead extends Card {

    public TymaretCallsTheDead() {
        CardAnyOfPredicate creatureOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        ExileOwnGraveyardCardThenCreateTokenEffect exileAndCreateZombie =
                new ExileOwnGraveyardCardThenCreateTokenEffect(
                        creatureOrEnchantment, CreateTokenEffect.blackZombie(1));

        SequenceEffect chapterOneAndTwo = SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER), exileAndCreateZombie);
        addTrigger(Triggers.chapter(1), chapterOneAndTwo);
        addTrigger(Triggers.chapter(2), chapterOneAndTwo);

        PermanentCount zombies = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER);
        addTrigger(Triggers.chapter(3), SequenceEffect.of(
                new GainLifeEffect(zombies), new ScryEffect(zombies)));
    }
}
