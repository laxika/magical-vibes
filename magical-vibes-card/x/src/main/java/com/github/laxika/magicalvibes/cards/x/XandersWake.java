package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "9")
public class XandersWake extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Thieves' Guild Enforcer",
            "Slaughter Specialist",
            "Acquisitions Expert",
            "Malakir Blood-Priest",
            "Boneclad Necromancer",
            "Tavern Swindler",
            "Blade Juggler",
            "Hoard Robber",
            "Morbid Opportunist",
            "Bloodthirsty Aerialist",
            "Asylum Visitor",
            "Yuan-Ti Fang-Blade",
            "Tithebearer Giant",
            "Malakir Cullblade",
            "Vengeful Warchief");

    public XandersWake() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new OncePerTurnTriggerEffect(new DraftCardFromSpellbookEffect(SPELLBOOK)));
    }
}
