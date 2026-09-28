package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TLE", collectorNumber = "37")
@CardRegistration(set = "SOC", collectorNumber = "260")
public class VolcanicTorrent extends Card {

    public VolcanicTorrent() {
        // Cascade: when you cast this spell, exile cards from the top of your library until you
        // exile a nonland card with lesser mana value, may cast it for free, and put the rest on
        // the bottom of your library in a random order.
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());

        // Volcanic Torrent deals damage to each creature and planeswalker opponents control equal
        // to the number of spells its controller has cast this turn.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new SpellsCastThisTurn(CountScope.CONTROLLER), false, true,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
