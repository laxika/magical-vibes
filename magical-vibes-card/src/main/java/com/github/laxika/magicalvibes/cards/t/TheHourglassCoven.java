package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "45")
public class TheHourglassCoven extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Hag of Syphoned Breath",
            "Hag of Dark Duress",
            "Hag of Ceaseless Torment",
            "Hag of Inner Weakness",
            "Hag of Death's Legion",
            "Hag of Scoured Thoughts",
            "Hag of Twisted Visions",
            "Hag of Mage's Doom",
            "Hag of Noxious Nightmares");

    public TheHourglassCoven() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DraftCardFromSpellbookEffect(SPELLBOOK, false, true),
                new DraftCardFromSpellbookEffect(SPELLBOOK, false, true)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WARLOCK))));
    }
}
