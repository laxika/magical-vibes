package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "162")
public class ClanGuildmage extends Card {

    public ClanGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{1}{R}, {T}: Target creature can't block this turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new AnimatePermanentsEffect(
                        4, 4,
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN
                )),
                "{2}{G}, {T}: Target land you control becomes a 4/4 Elemental creature with haste until end of turn. It's still a land.",
                TargetFilters.landYouControl()
        ));
    }
}
