package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "47")
public class GarrukWrathOfTheWilds extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Mosscoat Goriak",
            "Sylvan Brushstrider",
            "Murasa Rootgrazer",
            "Dire Wolf Prowler",
            "Ferocious Pup",
            "Pestilent Wolf",
            "Garruk's Uprising",
            "Dawntreader Elk",
            "Nessian Hornbeetle",
            "Territorial Scythecat",
            "Trufflesnout",
            "Wary Okapi",
            "Scurrid Colony",
            "Barkhide Troll",
            "Underdark Basilisk");

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
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)),
                "−6: Until end of turn, creatures you control get +3/+3 and gain trample."));
    }
}
