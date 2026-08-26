package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "219")
public class BorborygmosAndFblthp extends Card {

    public BorborygmosAndFblthp() {
        SequenceEffect drawAndDiscard = SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardAnyNumberThenEffect(
                        new CardTypePredicate(CardType.LAND),
                        new DealDamageToTargetCreatureEffect(new Scaled(new EventValue(), 2)),
                        "land cards"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, drawAndDiscard);
        addEffect(EffectSlot.ON_ATTACK, drawAndDiscard);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(PutTargetPermanentIntoLibraryNFromTopEffect.self(2)),
                "{1}{U}: Put Borborygmos and Fblthp into its owner's library third from the top."));
    }
}
