package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

/**
 * Driven // Despair — front half (Driven).
 * Sorcery — Until end of turn, creatures you control gain trample and
 * "Whenever this creature deals combat damage to a player, draw a card."
 * Back half (Despair) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "157")
public class DrivenDespair extends Card {

    public DrivenDespair() {
        Despair despair = new Despair();
        despair.setSetCode(getSetCode());
        despair.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(despair);

        // Until end of turn, creatures you control gain trample and
        // "Whenever this creature deals combat damage to a player, draw a card."
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SPELL, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1)));
    }

    @Override
    public String getBackFaceClassName() {
        return "Despair";
    }
}
