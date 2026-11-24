package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "13")
public class GoblinInfluxArray extends Card {

    private static final List<SpellbookCard> SPELLBOOK = List.of(
            new SpellbookCard("DOM", "130"),
            new SpellbookCard("M10", "139"),
            new SpellbookCard("DOM", "144"),
            new SpellbookCard("M21", "133"),
            new SpellbookCard("XLN", "174"),
            new SpellbookCard("M19", "144"),
            new SpellbookCard("M11", "135"),
            new SpellbookCard("ZNR", "153"),
            new SpellbookCard("RIX", "101"),
            new SpellbookCard("M12", "136"),
            new SpellbookCard("J21", "21"),
            new SpellbookCard("AFR", "132"),
            new SpellbookCard("VMA", "150"),
            new SpellbookCard("M19", "142"),
            new SpellbookCard("GRN", "109"));

    public GoblinInfluxArray() {
        addStaticEffect(new ReduceColoredCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.GOBLIN),
                new ManaCost("{R}"),
                CostModificationScope.SELF,
                true));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                ConjureCardsEffect.atRandomFrom(SPELLBOOK, ConjureDestination.intoHand()));
    }
}
