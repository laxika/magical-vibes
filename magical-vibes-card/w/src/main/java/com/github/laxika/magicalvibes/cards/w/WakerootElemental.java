package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "202")
public class WakerootElemental extends Card {

    public WakerootElemental() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}{G}{G}{G}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsLandPredicate()),
                        new AnimatePermanentsEffect(
                                5, 5,
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.HASTE),
                                null, Set.of(),
                                GrantScope.TARGET, EffectDuration.PERMANENT
                        )
                ),
                "{G}{G}{G}{G}{G}: Untap target land you control. It becomes a 5/5 Elemental creature with haste. It's still a land.",
                TargetFilters.landYouControl()
        ));
    }
}
