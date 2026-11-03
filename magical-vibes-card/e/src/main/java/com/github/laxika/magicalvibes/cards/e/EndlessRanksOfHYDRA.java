package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "43")
@CardRegistration(set = "MSC", collectorNumber = "348")
public class EndlessRanksOfHYDRA extends Card {

    public EndlessRanksOfHYDRA() {
        addSpellEffect(new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)), "Villain", 2, 1,
                CardColor.BLACK, List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of()));

        var returnToHand = new MayPayManaEffect(
                "{1}{B}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                "Pay {1}{B} to return this card from your graveyard to your hand?");

        addTrigger(new TriggerCondition(Triggers.enters(new PermanentIsCommanderPredicate(true), ObjectRelation.ANY),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                returnToHand);
        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new ConditionalEffect(
                        new HasAttacker(new PermanentIsCommanderPredicate(true)), returnToHand));
    }
}
