package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.OpponentDiscardsAnyNumberThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "101")
public class FerventMastery extends Card {

    public FerventMastery() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(), new OpponentDiscardsAnyNumberThenDrawsThatManyEffect()));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(3), null, LibrarySearchDestination.HAND));
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.CONTROLLER, true));
    }
}
