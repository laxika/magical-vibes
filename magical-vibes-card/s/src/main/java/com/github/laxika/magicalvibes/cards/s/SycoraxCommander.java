package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "161")
@CardRegistration(set = "WHO", collectorNumber = "445")
@CardRegistration(set = "WHO", collectorNumber = "766")
@CardRegistration(set = "WHO", collectorNumber = "1036")
public class SycoraxCommander extends Card {

    public static final String DISCARD_OPTION =
            "That opponent discards all the cards in their hand, then draws that many cards minus one";
    public static final String DAMAGE_OPTION =
            "This creature deals damage to that player equal to the number of cards in their hand";

    public SycoraxCommander() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(Subjects.eachOpponent(), List.of(
                        new ChooseOneEffect.ChooseOneOption(DISCARD_OPTION,
                                new TargetPlayerDiscardsHandThenDrawsThatManyEffect(1)),
                        new ChooseOneEffect.ChooseOneOption(DAMAGE_OPTION,
                                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                                        Subjects.targetPlayerDeclaredElsewhere())))));
    }
}
