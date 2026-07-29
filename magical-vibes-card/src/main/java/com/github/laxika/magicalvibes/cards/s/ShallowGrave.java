package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Shallow Grave — {@code {1}{B}} instant.
 *
 * <p>"Return the top creature card of your graveyard to the battlefield. That creature gains haste
 * until end of turn. Exile it at the beginning of the next end step."
 *
 * <p>Non-targeting: the topmost (most recently placed) creature card is forced, so {@code topmost}
 * resolves it without a prompt. Unlike Unearth there is no "exile it instead if it would leave the
 * battlefield" clause, so {@code exileIfLeavesBattlefield} stays off.
 */
@CardRegistration(set = "MIR", collectorNumber = "141")
public class ShallowGrave extends Card {

    public ShallowGrave() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .topmost(true)
                .grantHaste(true)
                .exileAtEndStep(true)
                .build());
    }
}
