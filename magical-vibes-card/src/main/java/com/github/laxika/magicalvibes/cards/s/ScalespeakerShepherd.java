package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "21")
public class ScalespeakerShepherd extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Ancient Imperiosaur",
            "Burning Sun's Avatar",
            "Carnage Tyrant",
            "Charging Monstrosaur",
            "Etali, Primal Conqueror",
            "Ghalta, Primal Hunger",
            "Gishath, Sun's Avatar",
            "Quartzwood Crasher",
            "Regisaur Alpha",
            "Ripjaw Raptor",
            "Shifting Ceratops",
            "Territorial Allosaurus",
            "Tranquil Frillback",
            "Verdant Sun's Avatar",
            "Zacama, Primal Calamity");

    public ScalespeakerShepherd() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DraftCardFromSpellbookEffect(SPELLBOOK));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DINOSAUR), 1, CostModificationScope.SELF));
    }
}
