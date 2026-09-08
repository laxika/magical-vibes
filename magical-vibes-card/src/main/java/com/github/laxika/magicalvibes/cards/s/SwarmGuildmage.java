package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "201")
public class SwarmGuildmage extends Card {

    public SwarmGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{B}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES)
                ),
                "{4}{B}, {T}: Creatures you control get +1/+0 and gain menace until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new GainLifeEffect(2)),
                "{1}{G}, {T}: You gain 2 life."
        ));
    }
}
