package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBlockOrBecomesBlockedTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "55")
@CardRegistration(set = "FEM", collectorNumber = "113")
public class GoblinFlotilla extends Card {

    public GoblinFlotilla() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{R}"),
                        List.of(new RegisterDelayedBlockOrBecomesBlockedTriggerEffect(
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET))),
                        true));
    }
}
