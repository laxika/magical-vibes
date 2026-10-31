package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "271")
public class TouchOfVitae extends Card {

    public TouchOfVitae() {
        // "{0}: Untap this creature. Activate only once." — granted until end of turn.
        ActivatedAbility grantedAbility = new ActivatedAbility(false, "{0}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{0}: Untap this creature. Activate only once.")
                .withMaxActivationsPerGame(1);

        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addSpellEffect(new GrantActivatedAbilityEffect(
                        grantedAbility, GrantScope.TARGET, null, EffectDuration.UNTIL_END_OF_TURN))
                .addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
