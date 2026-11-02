package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "205")
public class AvengersUnderSiege extends Card {

    public AvengersUnderSiege() {
        addTrigger(Triggers.chapter(1),
                new CreateTokenEffect(2, "Villain", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of()));

        var nonVillainCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN))));
        addSimultaneousTrigger(Triggers.chapter(2),
                new MassDamageEffect(2, false, false, nonVillainCreature),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));

        addTrigger(Triggers.chapter(3),
                CreateTokenEffect.ofTreasureToken(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.VILLAIN), CountScope.CONTROLLER)));
    }
}
