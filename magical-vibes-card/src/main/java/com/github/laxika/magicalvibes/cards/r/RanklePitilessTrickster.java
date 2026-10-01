package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsNoPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "11")
public class RanklePitilessTrickster extends Card {

    public RanklePitilessTrickster() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsNoPermanent(new PermanentIsCreaturePredicate()),
                new GrantKeywordEffect(Set.of(Keyword.HASTE, Keyword.LIFELINK), GrantScope.SELF)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayPayLifeEffect(
                1,
                SequenceEffect.of(
                        new DiscardEffect(1, DiscardRecipient.EACH_PLAYER),
                        new EachPlayerSacrificesCreatureEffect()),
                "Pay 1 life to have each player discard a card and sacrifice a creature?"));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new PerpetuallyBoostSourceEffect(1, 0));
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new PerpetuallyBoostSourceEffect(1, 0));
    }
}
