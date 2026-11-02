package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "150")
@CardRegistration(set = "DDT", collectorNumber = "43")
public class GoblinGloryChaser extends Card {

    public GoblinGloryChaser() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));

        // As long as this creature is renowned, it has menace.
        addStaticEffect(new ConditionalEffect(
                new SourceIsRenowned(), new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)));
    }
}
