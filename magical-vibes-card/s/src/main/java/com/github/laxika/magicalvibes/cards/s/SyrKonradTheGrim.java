package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "107")
@CardRegistration(set = "SLD", collectorNumber = "1297")
@CardRegistration(set = "FCA", collectorNumber = "10")
@CardRegistration(set = "MOC", collectorNumber = "269")
@CardRegistration(set = "DSC", collectorNumber = "158")
@CardRegistration(set = "M3C", collectorNumber = "207")
@CardRegistration(set = "MKC", collectorNumber = "141")
@CardRegistration(set = "ZNC", collectorNumber = "56")
public class SyrKonradTheGrim extends Card {

    public SyrKonradTheGrim() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new MillEffect(1, MillRecipient.CONTROLLER),
                        new MillEffect(1, MillRecipient.EACH_OPPONENT)),
                "{1}{B}: Each player mills a card."
        ));
    }
}
