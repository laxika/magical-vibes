package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

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
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProliferateEffect());
        addEffect(EffectSlot.ON_CONTROLLER_PROLIFERATES,
                new OncePerTurnTriggerEffect(
                        new ConditionalEffect(new ControllerTurn(),
                                new DraftCardFromSpellbookEffect(SPELLBOOK))));
    }
}
