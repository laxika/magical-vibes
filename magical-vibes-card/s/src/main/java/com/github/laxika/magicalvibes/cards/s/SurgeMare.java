package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "77")
public class SurgeMare extends Card {

    public SurgeMare() {
        // "This creature can't be blocked by green creatures."
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));

        // "Whenever this creature deals damage to an opponent, you may draw a card. If you do, discard a card."
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new MayEffect(
                SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Draw a card and discard a card?"));

        // "{1}{U}: This creature gets +2/-2 until end of turn."
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(new BoostSelfEffect(2, -2)),
                "{1}{U}: Surge Mare gets +2/-2 until end of turn."));
    }
}
