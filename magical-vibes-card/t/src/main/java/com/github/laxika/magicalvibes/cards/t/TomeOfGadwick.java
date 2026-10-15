package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "6")
public class TomeOfGadwick extends Card {

    public TomeOfGadwick() {
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new Fixed(1), new Fixed(0), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(1),
                GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK, new ConjureRandomCardFromSpellbookToHandEffect(List.of(
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("A25", "46"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("M10", "68"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("TOR", "43"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("MID", "44"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("WOE", "67"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("M11", "70"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("XLN", "65"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("10E", "94"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("DKA", "52"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("5DN", "36"),
                new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("M14", "68")
        )));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
