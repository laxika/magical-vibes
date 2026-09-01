package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "EOE", collectorNumber = "59")
public class IllvoiInfiltrator extends Card {

    public IllvoiInfiltrator() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastTwoOrMoreSpellsThisTurn(new CardTruePredicate()),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
    }
}
