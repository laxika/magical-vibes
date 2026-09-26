package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "28")
public class RecruitInstructor extends Card {

    public RecruitInstructor() {
        addEffect(EffectSlot.ON_ATTACK, new DraftCardFromSpellbookToHandEffect(List.of(
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("INR", "229"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "112"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("WOE", "317"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("SNC", "4"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("WOE", "7"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("BLB", "8"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("KTK", "7"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "120"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("BLB", "13"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("BLB", "21"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("BLB", "144"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ISD", "24"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("M21", "170"),
                new DraftCardFromSpellbookToHandEffect.CardPrintingReference("BLB", "160")
        )));

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new TriggeringSpellControllerConditionalEffect(new OncePerTurnTriggerEffect(
                        new CreateTokenEffect(1, "Mouse", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.MOUSE), Set.of(), Set.of()))));
    }
}
