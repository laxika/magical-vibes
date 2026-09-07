package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "36")
public class ValleyQuestcaller extends Card {

    public ValleyQuestcaller() {
        Set<CardSubtype> valleyCreatures = Set.of(
                CardSubtype.RABBIT, CardSubtype.BAT, CardSubtype.BIRD, CardSubtype.MOUSE);
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(valleyCreatures)));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.RABBIT),
                                new CardSubtypePredicate(CardSubtype.BAT),
                                new CardSubtypePredicate(CardSubtype.BIRD),
                                new CardSubtypePredicate(CardSubtype.MOUSE))),
                        new ScryEffect(1)));
    }
}
