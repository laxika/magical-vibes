package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "19")
public class ContagionDispenser extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Bloom Hulk",
            "Cankerbloom",
            "Contagion Clasp",
            "Contagion Engine",
            "Contentious Plan",
            "Evolution Sage",
            "Flux Channeler",
            "Inexorable Tide",
            "Merfolk Skydiver",
            "Pollenbright Druid",
            "Roalesk, Apex Hybrid",
            "Smell Fear",
            "Sword of Truth and Justice",
            "Tezzeret's Gambit",
            "Thrummingbird");

    public ContagionDispenser() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProliferateEffect());
        addEffect(EffectSlot.ON_CONTROLLER_PROLIFERATES,
                new OncePerTurnTriggerEffect(
                        new ConditionalEffect(new ControllerTurn(),
                                new DraftCardFromSpellbookEffect(SPELLBOOK))));
    }
}
