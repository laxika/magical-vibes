package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "137")
public class ReignOfTerror extends Card {

    public ReignOfTerror() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Destroy all green creatures", wipe(CardColor.GREEN)),
                new ChooseOneEffect.ChooseOneOption("Destroy all white creatures", wipe(CardColor.WHITE))
        )));
    }

    private static DestroyAllPermanentsEffect wipe(final CardColor color) {
        final PermanentPredicate filter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))
        ));
        return new DestroyAllPermanentsEffect(filter, true, EachPermanentScope.ALL_PLAYERS,
                new LoseLifeEffect(new Scaled(new EventValue(), 2), LoseLifeRecipient.CONTROLLER), false);
    }
}
