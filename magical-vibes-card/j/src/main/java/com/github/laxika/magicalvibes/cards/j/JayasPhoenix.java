package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextLoyaltyAbilityThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "738")
@CardRegistration(set = "CMM", collectorNumber = "768")
public class JayasPhoenix extends Card {

    public JayasPhoenix() {
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                new CopyNextLoyaltyAbilityThisTurnEffect());
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.PLANESWALKER),
                List.of(new MayEffect(
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Return Jaya's Phoenix from your graveyard to the battlefield?"))
        ));
    }
}
