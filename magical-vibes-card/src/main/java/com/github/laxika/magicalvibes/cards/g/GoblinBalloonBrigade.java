package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "140")
public class GoblinBalloonBrigade extends Card {

    public GoblinBalloonBrigade() {
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{R}: Goblin Balloon Brigade gains flying until end of turn."));
    }
}
