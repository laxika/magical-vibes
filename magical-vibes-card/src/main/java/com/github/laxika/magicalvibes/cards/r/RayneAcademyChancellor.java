package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "UDS", collectorNumber = "43")
public class RayneAcademyChancellor extends Card {

    public RayneAcademyChancellor() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new ConditionalEffect(new Enchanted(),
                        new MayEffect(new DrawCardEffect(), "Draw an additional card?")));
    }
}
