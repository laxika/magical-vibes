package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH2", collectorNumber = "69")
@CardRegistration(set = "MH2", collectorNumber = "310")
public class SvyelunOfSeaAndSky extends Card {

    public SvyelunOfSeaAndSky() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsOtherPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect());

        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(1),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)));
    }
}
