package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInExileOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileChosenPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "143")
@CardRegistration(set = "WHO", collectorNumber = "426")
@CardRegistration(set = "WHO", collectorNumber = "542")
@CardRegistration(set = "WHO", collectorNumber = "748")
@CardRegistration(set = "WHO", collectorNumber = "1017")
@CardRegistration(set = "WHO", collectorNumber = "1133")
public class TheMasterFormedAnew extends Card {

    public TheMasterFormedAnew() {
        addTrigger(Triggers.youCastThisSpell(), new MayEffect(
                SequenceEffect.of(
                        ExileChosenPermanentsEffect.youControl(new PermanentIsCreaturePredicate(), "creature"),
                        PutCounterOnExiledCardsEffect.onCardsExiledThisWay(CounterType.TAKEOVER)),
                "Exile a creature you control and put a takeover counter on it?"));
        addStaticEffect(new CopyCreatureCardInExileOnEnterEffect());
    }
}
