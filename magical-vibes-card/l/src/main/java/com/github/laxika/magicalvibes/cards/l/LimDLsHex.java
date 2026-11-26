package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "146")
public class LimDLsHex extends Card {

    public LimDLsHex() {
        // At the beginning of your upkeep, for each player, this enchantment deals 1 damage to that player unless they
        // pay {B} or {3} (monocolored hybrid {B/3} is that choice): every player chooses in APNAP order, then the
        // damage is dealt to those who didn't pay at once (CR 101.4).
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.eachUnless(Subjects.eachPlayer(),
                new PayManaCost("{B/3}"), players -> DealDamageEffect.to(players, 1)));
    }
}
