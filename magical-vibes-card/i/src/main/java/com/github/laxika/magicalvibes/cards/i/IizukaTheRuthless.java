package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "104")
public class IizukaTheRuthless extends Card {

    public IizukaTheRuthless() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(2));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                                "Sacrifice a Samurai",
                                false),
                        new GrantKeywordEffect(
                                Keyword.DOUBLE_STRIKE,
                                GrantScope.ALL_OWN_CREATURES,
                                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI))),
                "{2}{R}, Sacrifice a Samurai: Samurai creatures you control gain double strike until end of turn."
        ));
    }
}
