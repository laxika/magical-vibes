package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "185")
@CardRegistration(set = "MKM", collectorNumber = "355")
@CardRegistration(set = "MKM", collectorNumber = "384")
public class AlquistProftMasterSleuth extends Card {

    public AlquistProftMasterSleuth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofClueToken(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{W}{U}{U}",
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.CLUE), "a Clue"),
                        new DrawCardEffect(new XValue()),
                        new GainLifeEffect(new XValue())
                ),
                "{X}{W}{U}{U}, {T}, Sacrifice a Clue: You draw X cards and gain X life."
        ));
    }
}
