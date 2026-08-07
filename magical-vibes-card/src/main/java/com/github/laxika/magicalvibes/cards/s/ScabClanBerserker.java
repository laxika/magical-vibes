package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "160")
public class ScabClanBerserker extends Card {

    public ScabClanBerserker() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));

        // Whenever an opponent casts a noncreature spell, if this creature is renowned, this
        // creature deals 2 damage to that player.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastDamageToCasterEffect(2,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), new SourceIsRenowned()));
    }
}
