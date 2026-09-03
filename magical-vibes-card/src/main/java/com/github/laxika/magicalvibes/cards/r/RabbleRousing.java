package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "24")
public class RabbleRousing extends Card {

    public RabbleRousing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(5, true));

        CreateTokenEffect citizen = new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Citizen", 1, 1,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.CITIZEN), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, SequenceEffect.of(
                citizen,
                new ConditionalEffect(new ControlsPermanentCount(10, new PermanentIsCreaturePredicate()),
                        new PlayImprintedCardWithoutPayingManaCostEffect())));
    }
}
