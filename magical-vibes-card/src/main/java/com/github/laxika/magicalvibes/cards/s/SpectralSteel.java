package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "30")
public class SpectralSteel extends Card {

    public SpectralSteel() {
        // Enchant creature
        target(TargetFilters.creature()).addEffect(
                EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE));

        // {1}{W}, Exile this card from your graveyard: Return another target Aura or Equipment card
        // from your graveyard to your hand.
        CardPredicate anotherAuraOrEquipment = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.AURA),
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT))),
                new CardNotPredicate(new CardIsSelfPredicate())));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(anotherAuraOrEquipment)
                ),
                "{1}{W}, Exile this card from your graveyard: Return another target Aura or Equipment "
                        + "card from your graveyard to your hand."
        ));
    }
}
