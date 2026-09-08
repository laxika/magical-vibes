package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseCastCostForChosenNameSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "15")
public class CurseOfSilence extends Card {

    public CurseOfSilence() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect());
        addEffect(EffectSlot.STATIC, new IncreaseCastCostForChosenNameSpellsEffect(2));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(new SacrificeSelfThenEffect(new DrawCardEffect())),
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntrySharesChosenNameWithSourcePredicate(),
                                new StackEntryControlledByEnchantedPlayerPredicate()))),
                "Sacrifice Curse of Silence to draw a card?"));
    }
}
