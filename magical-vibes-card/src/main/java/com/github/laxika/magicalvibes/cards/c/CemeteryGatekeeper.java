package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "148")
public class CemeteryGatekeeper extends Card {

    public CemeteryGatekeeper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());

        CardSharesCardTypeWithImprintedCardPredicate sharedType =
                new CardSharesCardTypeWithImprintedCardPredicate(true);
        CardEffect damage = new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER);

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(sharedType, List.of(damage)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new TriggeringCardConditionalEffect(sharedType, damage));
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND,
                new TriggeringCardConditionalEffect(sharedType, damage));
    }
}
