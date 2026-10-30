package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "47")
public class GarrukWrathOfTheWilds extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "167"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "144"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "229"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "179"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "171"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "192"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "186"),
            new DraftFromSpellbookEffect.SpellbookCard("DKA", "111"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "182"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "213"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "212"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "149"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "142"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "165"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "208"));

    public GarrukWrathOfTheWilds() {
        // +1: Choose a creature card in your hand. It perpetually gets +1/+1 and perpetually gains
        // "This spell costs {1} less to cast."
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect(
                        new CardTypePredicate(CardType.CREATURE), 1, 1, 1)),
                "+1: Choose a creature card in your hand. It perpetually gets +1/+1 and perpetually gains "
                        + "\"This spell costs {1} less to cast.\""));

        // −1: Draft a card from Garruk, Wrath of the Wilds's spellbook and put it onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DraftCardFromSpellbookEffect(SPELLBOOK, false, true)),
                "−1: Draft a card from Garruk, Wrath of the Wilds's spellbook and put it onto the battlefield."));

        // −6: Until end of turn, creatures you control get +3/+3 and gain trample.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(
                        new BoostAllOwnCreaturesEffect(3, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES)),
                "−6: Until end of turn, creatures you control get +3/+3 and gain trample."));
    }
}
