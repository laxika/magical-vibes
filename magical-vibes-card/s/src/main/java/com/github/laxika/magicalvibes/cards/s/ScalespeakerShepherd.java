package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "21")
public class ScalespeakerShepherd extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MOM", "174"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "135"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "179"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "138"),
            new DraftFromSpellbookEffect.SpellbookCard("MOM", "137"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "130"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "222"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "201"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "227"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "203"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "194"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "184"),
            new DraftFromSpellbookEffect.SpellbookCard("MAT", "24"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "213"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "174"));

    public ScalespeakerShepherd() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DraftCardFromSpellbookEffect(SPELLBOOK));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DINOSAUR), 1, CostModificationScope.SELF));
    }
}
