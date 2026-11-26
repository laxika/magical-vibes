package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "158")
@CardRegistration(set = "THB", collectorNumber = "323")
@CardRegistration(set = "DSC", collectorNumber = "168")
@CardRegistration(set = "AFC", collectorNumber = "144")
@CardRegistration(set = "SCD", collectorNumber = "161")
public class TectonicGiant extends Card {

    public TectonicGiant() {
        addTrigger(Triggers.attacks(), chooseMode());
        addTrigger(Triggers.becomesTargetOfOpponentSpell(), chooseMode());
    }

    private ChooseOneEffect chooseMode() {
        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "This creature deals 3 damage to each opponent.",
                        DealDamageEffect.to(Subjects.eachOpponent(), 3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Choose one of them. Until the end of your next turn, you may play that card.",
                        new ExileTopCardMayPlayThisTurnEffect(2, false)
                                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).choosingOne())
        ));
    }
}
