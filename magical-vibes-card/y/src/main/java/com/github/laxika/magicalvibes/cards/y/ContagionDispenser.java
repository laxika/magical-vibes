package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "19")
public class ContagionDispenser extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "154"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "161"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "144"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "145"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "46"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "159"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "52"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "35"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "205"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "173"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "213"),
            new DraftFromSpellbookEffect.SpellbookCard("MH2", "173"),
            new DraftFromSpellbookEffect.SpellbookCard("MH1", "229"),
            new DraftFromSpellbookEffect.SpellbookCard("NPH", "47"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "47"));

    public ContagionDispenser() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ProliferateEffect());
        addTrigger(Triggers.youProliferate(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn())
                        .withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
