package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsIntoTopOfLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YBLB", collectorNumber = "3")
public class SandcloudHarbinger extends Card {

    public SandcloudHarbinger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardsIntoTopOfLibrariesEffect("AKH", "249", 3, 10));

        CardSubtypePredicate desert = new CardSubtypePredicate(CardSubtype.DESERT);
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new TriggeringCardConditionalEffect(desert,
                        SequenceEffect.of(new DrawCardEffect(1), new GainLifeEffect(3))));
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND,
                new TriggeringCardConditionalEffect(desert,
                        SequenceEffect.of(new DrawCardForTargetPlayerEffect(1), new GainLifeEffect(3))));
    }
}
