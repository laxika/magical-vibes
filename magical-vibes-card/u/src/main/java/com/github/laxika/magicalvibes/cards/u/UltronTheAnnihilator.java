package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "668")
public class UltronTheAnnihilator extends Card {

    private static final CreateTokenEffect ROBOT_TOKEN = new CreateTokenEffect(
            1, "Robot", 2, 2, null,
            List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN), Set.of(), Set.of(CardType.ARTIFACT));
    private static final LoseLifeEffect DRAIN = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);

    public UltronTheAnnihilator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ROBOT_TOKEN);
        addTrigger(Triggers.attacks(), ROBOT_TOKEN);
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        ObjectRelation.YOU_OWN),
                DRAIN);
        addTrigger(Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN),
                DRAIN);
    }
}
