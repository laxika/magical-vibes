package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1628")
@CardRegistration(set = "CMM", collectorNumber = "92")
@CardRegistration(set = "CMM", collectorNumber = "488")
@CardRegistration(set = "C16", collectorNumber = "8")
public class FaerieArtisans extends Card {

    public FaerieArtisans() {
        CreateTokenCopyOfTargetPermanentEffect copyEffect = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(CardType.ARTIFACT), null, null, Map.of(), false, false, false, false,
                true, false, null, Set.of(), false, List.of(), List.of(), false, false,
                new Fixed(1), false, Set.of(), false).ofTriggeringPermanent();
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.OPPONENT_CONTROLS),
                new ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect(copyEffect));
    }
}
