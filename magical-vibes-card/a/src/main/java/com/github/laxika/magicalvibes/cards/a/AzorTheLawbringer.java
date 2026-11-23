package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeAndDrawXCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "154")
public class AzorTheLawbringer extends Card {

    public AzorTheLawbringer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new PlayerRestrictionEffect(
                Subjects.eachOpponent(), PlayerRestriction.castingSpellsOf(CardType.INSTANT, CardType.SORCERY),
                Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
        addTrigger(Triggers.attacks(), new PayXManaGainXLifeAndDrawXCardsEffect("{X}{W}{U}{U}"));
    }
}
