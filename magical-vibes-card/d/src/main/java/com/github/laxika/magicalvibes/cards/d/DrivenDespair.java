package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Driven // Despair — front half (Driven).
 * Sorcery — Until end of turn, creatures you control gain trample and
 * "Whenever this creature deals combat damage to a player, draw a card."
 * Back half (Despair) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "157")
@CardRegistration(set = "AKR", collectorNumber = "233")
public class DrivenDespair extends Card {

    public DrivenDespair() {
        setBackFaceCard(new Despair());

        // Until end of turn, creatures you control gain trample and
        // "Whenever this creature deals combat damage to a player, draw a card."
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addSpellEffect(new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1))));
    }

    @Override
    public String getBackFaceClassName() {
        return "Despair";
    }
}
