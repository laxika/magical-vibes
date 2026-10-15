package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YTDM", collectorNumber = "14")
public class SwiftspearsTeachings extends Card {

    public SwiftspearsTeachings() {
        addEffect(EffectSlot.SPELL, RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                new CardTypePredicate(CardType.CREATURE), List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("It gains prowess",
                                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.PROWESS))),
                        new ChooseOneEffect.ChooseOneOption("It gains haste",
                                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE)))
                )))));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
