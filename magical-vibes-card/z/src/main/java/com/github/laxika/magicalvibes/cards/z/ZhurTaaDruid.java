package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * {@code {T}: Add {G}.} plus "Whenever you tap this creature for mana, it deals 1 damage to each
 * opponent." The damage rider is a separate triggered ability, not part of the mana ability, so it
 * uses the stack once a player would next receive priority.
 */
@CardRegistration(set = "DGM", collectorNumber = "120")
@CardRegistration(set = "DDL", collectorNumber = "48")
@CardRegistration(set = "GK2", collectorNumber = "101")
@CardRegistration(set = "C16", collectorNumber = "232")
public class ZhurTaaDruid extends Card {

    public ZhurTaaDruid() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addTrigger(Triggers.tappedForMana(),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
