package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "24")
public class CurseboundWitch extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Witch's Cauldron",
            "Witch's Cottage",
            "Cauldron Familiar",
            "Bloodhunter Bat",
            "Cruel Reality",
            "Witch's Vengeance",
            "Witch's Familiar",
            "Black Cat",
            "Unwilling Ingredient",
            "Torment of Scarabs",
            "Witch's Oven",
            "Curse of Leeches",
            "Sorcerer's Broom",
            "Expanded Anatomy",
            "Trespasser's Curse");

    public CurseboundWitch() {
        addEffect(EffectSlot.ON_DEATH, new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
