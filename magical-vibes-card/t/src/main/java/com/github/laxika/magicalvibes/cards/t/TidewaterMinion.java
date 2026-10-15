package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "71")
public class TidewaterMinion extends Card {

    public TidewaterMinion() {
        // {4}: This creature loses defender until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF)),
                "{4}: This creature loses defender until end of turn."
        ));

        // {T}: Untap target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Untap target permanent.",
                TargetFilters.permanent()
        ));
    }
}
