package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToSourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "27a")
@CardRegistration(set = "HML", collectorNumber = "27b")
public class GiantAlbatross extends Card {

    public GiantAlbatross() {
        // When this creature dies, you may pay {1}{U}. If you do, for each creature that dealt damage
        // to this creature this turn, destroy that creature unless its controller pays 2 life. A
        // creature destroyed this way can't be regenerated.
        PermanentSubject damagers = Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentDealtDamageToSourceThisTurnPredicate())));
        addTrigger(Triggers.dies(ObjectRelation.THIS), UnlessPaysEffect.youMayPay("{1}{U}",
                        UnlessPaysEffect.forEach(damagers, Subjects.controllerOf(damagers), new PayLifeCost(2),
                                unpaid -> new DestroyTargetPermanentEffect(unpaid, true)))
                .withPrompt("Pay {1}{U} to punish the creatures that damaged this creature?"));
    }
}
