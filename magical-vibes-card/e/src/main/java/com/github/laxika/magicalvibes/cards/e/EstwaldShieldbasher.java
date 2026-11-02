package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "11")
@CardRegistration(set = "DBL", collectorNumber = "278")
public class EstwaldShieldbasher extends Card {

    public EstwaldShieldbasher() {
        addTrigger(Triggers.attacks(), new MayPayManaEffect(
                "{1}",
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                "Pay {1} to give Estwald Shieldbasher indestructible until end of turn?"
        ));
    }
}
