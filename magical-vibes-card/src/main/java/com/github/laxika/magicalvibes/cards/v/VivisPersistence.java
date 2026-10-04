package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "458")
public class VivisPersistence extends Card {

    public VivisPersistence() {
        Map<EffectSlot, CardEffect> wizardTokenEffects = Map.of(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))));

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
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

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCommanderPredicate(), returnToHand));
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new HasAttacker(new PermanentIsCommanderPredicate()), returnToHand));
    }
}
