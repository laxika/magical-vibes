package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "458")
public class VivisPersistence extends Card {

    public VivisPersistence() {
        List<Ability> wizardTokenEffects = List.of(
                new TriggeredAbility(Triggers.youCastSpell()
                                .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)));

        addSpellEffect(new CreateTokenEffect(
                1, "Wizard", 0, 1, CardColor.BLACK, List.of(CardSubtype.WIZARD),
                Set.of(), Set.of(), wizardTokenEffects));

        var returnToHand = new MayPayManaEffect(
                "{2}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                "Pay {2} to return this card from your graveyard to your hand?");

        addTrigger(new TriggerCondition(Triggers.enters(new PermanentIsCommanderPredicate(true), ObjectRelation.ANY),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                returnToHand);
        addTrigger(new TriggerCondition(Triggers.creatureAttacks()
                                .withFilter(new PermanentIsCommanderPredicate(true)),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                returnToHand);
    }
}
