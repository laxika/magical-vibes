package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "163")
public class WarrenTorchmaster extends Card {

    public WarrenTorchmaster() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                new BlightEffect(1,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET,
                                new PermanentIsCreaturePredicate()),
                        true),
                "Blight 1?"));
    }
}
