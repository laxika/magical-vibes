package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeEnchantmentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "265")
public class HiddenStag extends Card {

    public HiddenStag() {
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND, new TriggeringCardConditionalEffect(
                new CardTruePredicate(),
                new ConditionalEffect(new SourceIsEnchantment(),
                        new BecomeCreatureEffect(3, 2, List.of(CardSubtype.ELK, CardSubtype.BEAST))
                )));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new TriggeringCardConditionalEffect(
                new CardTruePredicate(),
                new ConditionalEffect(new SourceIsCreature(), new BecomeEnchantmentEffect(true))
        ));
    }
}
