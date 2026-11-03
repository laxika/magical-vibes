package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "134")
@CardRegistration(set = "DBL", collectorNumber = "134")
public class CurseOfShakenFaith extends Card {

    public CurseOfShakenFaith() {
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PLAYER);

        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryControlledByEnchantedPlayerPredicate())
                        .nthOrLaterThisTurn(2),
                damage);

        SpellCopyTriggerEffect copyTrigger = new SpellCopyTriggerEffect(
                null,
                List.of(damage),
                new StackEntryControlledByEnchantedPlayerPredicate(),
                true);
        addTrigger(Triggers.youCopySpell(), copyTrigger);
        addTrigger(Triggers.opponentCopiesSpell(), copyTrigger);
    }
}
