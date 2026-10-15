package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "234")
public class SilverquillSilencer extends Card {

    public SilverquillSilencer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardNameOnEnterEffect(List.of(CardType.LAND)));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER),
                        new DrawCardEffect()
                ),
                new StackEntrySharesChosenNameWithSourcePredicate()
        ));
    }
}
