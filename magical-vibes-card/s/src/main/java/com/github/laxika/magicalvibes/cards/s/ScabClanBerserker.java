package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "160")
@CardRegistration(set = "PIO", collectorNumber = "154")
public class ScabClanBerserker extends Card {

    public ScabClanBerserker() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));

        // Whenever an opponent casts a noncreature spell, if this creature is renowned, this
        // creature deals 2 damage to that player.
        addTrigger(Triggers.opponentCastsSpell(), new SpellCastDamageToCasterEffect(2,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), new SourceIsRenowned()));
    }
}
