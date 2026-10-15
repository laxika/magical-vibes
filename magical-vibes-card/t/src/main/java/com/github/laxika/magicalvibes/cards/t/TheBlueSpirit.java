package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TLE", collectorNumber = "90")
@CardRegistration(set = "TLE", collectorNumber = "178")
public class TheBlueSpirit extends Card {

    public TheBlueSpirit() {
        CardPredicate creatureSpell = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.STATIC, new GrantFlashToFirstMatchingSpellEachTurnEffect(creatureSpell));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardNotPredicate(new CardIsTokenPredicate()),
                        new ConditionalEffect(new DuringCombat(), new DrawCardEffect())));
    }
}
