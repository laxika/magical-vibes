package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "28")
public class RecruitInstructor extends Card {

    public RecruitInstructor() {
        addTrigger(Triggers.attacks(), new DraftCardFromSpellbookEffect(List.of(
                new DraftFromSpellbookEffect.SpellbookCard("INR", "229"),
                new DraftFromSpellbookEffect.SpellbookCard("ELD", "112"),
                new DraftFromSpellbookEffect.SpellbookCard("WOE", "317"),
                new DraftFromSpellbookEffect.SpellbookCard("SNC", "4"),
                new DraftFromSpellbookEffect.SpellbookCard("WOE", "7"),
                new DraftFromSpellbookEffect.SpellbookCard("BLB", "8"),
                new DraftFromSpellbookEffect.SpellbookCard("KTK", "7"),
                new DraftFromSpellbookEffect.SpellbookCard("ELD", "120"),
                new DraftFromSpellbookEffect.SpellbookCard("BLB", "13"),
                new DraftFromSpellbookEffect.SpellbookCard("BLB", "21"),
                new DraftFromSpellbookEffect.SpellbookCard("BLB", "144"),
                new DraftFromSpellbookEffect.SpellbookCard("ISD", "24"),
                new DraftFromSpellbookEffect.SpellbookCard("M21", "170"),
                new DraftFromSpellbookEffect.SpellbookCard("BLB", "160")
        )));

        addTrigger(Triggers.becomesTargetOfYourSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateTokenEffect(1, "Mouse", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.MOUSE), Set.of(), Set.of()));
    }
}
