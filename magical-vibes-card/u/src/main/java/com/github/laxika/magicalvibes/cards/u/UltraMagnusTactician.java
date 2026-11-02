package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "15")
@CardRegistration(set = "BOT", collectorNumber = "29")
public class UltraMagnusTactician extends Card {

    public UltraMagnusTactician() {
        setBackFaceCard(new UltraMagnusArmoredCarrier());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{R}{G}{W}"));

        CardAllOfPredicate artifactCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)));
        addTrigger(Triggers.attacks(), new PutCardToBattlefieldThenEffect(
                artifactCreature,
                "artifact creature",
                true,
                true,
                null,
                new DelayedEndOfCombatEffect(new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "UltraMagnusArmoredCarrier";
    }
}
