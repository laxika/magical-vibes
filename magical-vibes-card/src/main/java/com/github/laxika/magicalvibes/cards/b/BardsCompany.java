package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "146")
public class BardsCompany extends Card {

    public BardsCompany() {
        setFlashCastCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));

        CardEffect recruit = SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardCardThenEffect(
                        null,
                        new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()),
                        "a card",
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, recruit);
        addEffect(EffectSlot.ON_ATTACK, recruit);
    }
}
