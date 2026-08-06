package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "196")
public class SkarrgGuildmage extends Card {

    public SkarrgGuildmage() {
        // {R}{G}: Creatures you control gain trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)),
                "{R}{G}: Creatures you control gain trample until end of turn."
        ));

        // {1}{R}{G}: Target land you control becomes a 4/4 Elemental creature until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{G}",
                List.of(new AnimatePermanentsEffect(
                        4, 4,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(),
                        null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN
                )),
                "{1}{R}{G}: Target land you control becomes a 4/4 Elemental creature until end of turn. It's still a land.",
                TargetFilters.landYouControl()
        ));
    }
}
