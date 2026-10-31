package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "52")
public class IshkanahBroodmother extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MH1", "188"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "98"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "173"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "156"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "172"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "132"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "171"),
            new DraftFromSpellbookEffect.SpellbookCard("M15", "186"),
            new DraftFromSpellbookEffect.SpellbookCard("M13", "189"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "176"),
            new DraftFromSpellbookEffect.SpellbookCard("ROE", "209"),
            new DraftFromSpellbookEffect.SpellbookCard("ISD", "204"),
            new DraftFromSpellbookEffect.SpellbookCard("ISD", "203"),
            new DraftFromSpellbookEffect.SpellbookCard("ISD", "200"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "159"));

    public IshkanahBroodmother() {
        // Other Spiders you control get +1/+2.
        addStaticEffect(new StaticBoostEffect(1, 2, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIDER)));

        // {1}{B/G}, Exile two cards from your graveyard: Draft a card from Ishkanah,
        // Broodmother's spellbook.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B/G}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)),
                "{1}{B/G}, Exile two cards from your graveyard: Draft a card from Ishkanah, "
                        + "Broodmother's spellbook."));
    }
}
