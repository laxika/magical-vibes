package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithFewerCreaturesThanController;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "107")
public class BirthOfTheImperium extends Card {

    public BirthOfTheImperium() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)),
                "Astartes Warrior", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.ASTARTES, CardSubtype.WARRIOR), Set.of(Keyword.VIGILANCE), Set.of()));

        addTrigger(Triggers.chapter(2), new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));

        addTrigger(Triggers.chapter(3), new DrawCardEffect(
                new Scaled(new OpponentsWithFewerCreaturesThanController(), 2)));
    }
}
