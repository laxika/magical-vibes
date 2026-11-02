package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsIntoTopOfLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "3")
public class SandcloudHarbinger extends Card {

    public SandcloudHarbinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureCardsIntoTopOfLibrariesEffect("AKH", "249", 3, 10));

        CardSubtypePredicate desert = new CardSubtypePredicate(CardSubtype.DESERT);
        addTrigger(Triggers.youPlayLand(),
                new TriggeringCardConditionalEffect(desert,
                        SequenceEffect.of(new DrawCardEffect(1), new GainLifeEffect(3))));
        addTrigger(Triggers.opponentPlaysLand(),
                new TriggeringCardConditionalEffect(desert,
                        SequenceEffect.of(new DrawCardForTargetPlayerEffect(1), new GainLifeEffect(3))));
    }
}
