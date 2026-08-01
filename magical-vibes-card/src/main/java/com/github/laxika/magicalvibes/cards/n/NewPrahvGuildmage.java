package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "181")
public class NewPrahvGuildmage extends Card {

    public NewPrahvGuildmage() {
        // {W}{U}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{W}{U}: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));

        // {3}{W}{U}: Detain target nonland permanent an opponent controls.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{U}",
                List.of(new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN, TargetCategory.PERMANENT)),
                "{3}{W}{U}: Detain target nonland permanent an opponent controls.",
                TargetFilters.nonlandPermanentAnOpponentControls()
        ));
    }
}
