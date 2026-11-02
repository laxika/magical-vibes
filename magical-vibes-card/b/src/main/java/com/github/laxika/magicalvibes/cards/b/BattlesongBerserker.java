package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "78")
public class BattlesongBerserker extends Card {

    public BattlesongBerserker() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.youAttack(), new BoostTargetCreatureEffect(1, 0))
                .addTrigger(Triggers.youAttack(), new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET));
    }
}
