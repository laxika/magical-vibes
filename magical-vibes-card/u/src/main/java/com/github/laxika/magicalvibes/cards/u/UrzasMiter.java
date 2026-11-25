package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ATQ", collectorNumber = "76")
@CardRegistration(set = "ME4", collectorNumber = "237")
public class UrzasMiter extends Card {

    public UrzasMiter() {
        addTrigger(Triggers.diesWithoutBeingSacrificed(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                UnlessPaysEffect.youMayPay("{3}", new DrawCardEffect()).withPrompt("Pay {3} to draw a card?"));
    }
}
