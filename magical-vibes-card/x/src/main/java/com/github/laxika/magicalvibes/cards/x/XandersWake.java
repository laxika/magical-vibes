package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "9")
public class XandersWake extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("M21", "125"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "122"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "89"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "110"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "93"),
            new DraftFromSpellbookEffect.SpellbookCard("RTR", "79"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "63"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "110"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "113"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "91"),
            new DraftFromSpellbookEffect.SpellbookCard("SOI", "99"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "128"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "107"),
            new DraftFromSpellbookEffect.SpellbookCard("ORI", "108"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "121"));

    public XandersWake() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new OncePerTurnTriggerEffect(new DraftCardFromSpellbookEffect(SPELLBOOK)));
    }
}
