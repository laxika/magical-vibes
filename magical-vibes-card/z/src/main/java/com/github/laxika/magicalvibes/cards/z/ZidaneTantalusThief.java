package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "251")
@CardRegistration(set = "FIN", collectorNumber = "405")
@CardRegistration(set = "FIN", collectorNumber = "518")
@CardRegistration(set = "FIN", collectorNumber = "550")
public class ZidaneTantalusThief extends Card {

    public ZidaneTantalusThief() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Set.of(Keyword.LIFELINK, Keyword.HASTE), GrantScope.TARGET));

        addTrigger(Triggers.opponentGainsControlOfPermanentYouControl(),
                CreateTokenEffect.ofTreasureToken(1));
    }
}
