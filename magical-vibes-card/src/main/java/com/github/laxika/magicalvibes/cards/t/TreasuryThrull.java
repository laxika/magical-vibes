package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "201")
public class TreasuryThrull extends Card {

    public TreasuryThrull() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                        "{W/B}"
                ),
                "Pay {W/B} to extort?"
        ));

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.ENCHANTMENT)
                        )))
                        .build(),
                "You may return target artifact, creature, or enchantment card from your graveyard to your hand."
        ));
    }
}
