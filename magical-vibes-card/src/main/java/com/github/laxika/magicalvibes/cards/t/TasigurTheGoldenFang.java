package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "87")
public class TasigurTheGoldenFang extends Card {

    public TasigurTheGoldenFang() {
        addEffect(EffectSlot.SPELL, new DelveCost());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G/U}{G/U}",
                List.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new OpponentChoosesCardFromGraveyardToHandEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))
                ),
                "{2}{G/U}{G/U}: Mill two cards, then return a nonland card from your graveyard to your hand.")
        );
    }
}
