package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "45")
public class PemminsAura extends Card {

    public PemminsAura() {
        target(TargetFilters.creature());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{U}: Untap enchanted creature."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE,
                        GrantDuration.END_OF_TURN)),
                "{U}: Enchanted creature gains flying until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_CREATURE,
                        GrantDuration.END_OF_TURN)),
                "{U}: Enchanted creature gains shroud until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Enchanted creature gets +1/-1 until end of turn",
                                new BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(1, -1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Enchanted creature gets -1/+1 until end of turn",
                                new BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(-1, 1))
                ))),
                "{1}: Enchanted creature gets +1/-1 or -1/+1 until end of turn."
        ));
    }
}
