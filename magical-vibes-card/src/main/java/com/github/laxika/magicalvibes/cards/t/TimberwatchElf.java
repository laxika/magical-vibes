package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "140")
@CardRegistration(set = "DD1", collectorNumber = "13")
@CardRegistration(set = "EVG", collectorNumber = "13")
public class TimberwatchElf extends Card {

    public TimberwatchElf() {
        PermanentCount elvesOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.ANY_PLAYER);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new BoostTargetCreatureEffect(elvesOnBattlefield, elvesOnBattlefield)),
                "{2}{G}, {T}: Target creature gets +X/+X until end of turn, where X is the number of Elves on the battlefield."
        ));
    }
}
