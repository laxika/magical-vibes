package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "26")
@CardRegistration(set = "VMA", collectorNumber = "48")
@CardRegistration(set = "UMA", collectorNumber = "37")
@CardRegistration(set = "C20", collectorNumber = "100")
public class SpiritCairn extends Card {

    public SpiritCairn() {
        UnlessPaysEffect trigger = UnlessPaysEffect.youMayPay("{W}", CreateTokenEffect.whiteSpirit(1))
                .withPrompt("Pay {W} to create a 1/1 white Spirit creature token with flying?");
        addTrigger(Triggers.opponentDiscardsCard(), trigger);
        addTrigger(Triggers.youDiscardCard(), trigger);
    }
}
