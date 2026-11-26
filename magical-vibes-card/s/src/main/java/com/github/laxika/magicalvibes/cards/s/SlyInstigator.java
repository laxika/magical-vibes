package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "32")
public class SlyInstigator extends Card {

    public SlyInstigator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        MakeCreatureUnblockableEffect.of(Subjects.target(), Duration.UNTIL_YOUR_NEXT_TURN),
                        new GoadTargetCreatureUntilNextTurnEffect()
                ),
                "{U}, {T}: Until your next turn, target creature an opponent controls can't be blocked. Goad that creature.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
