package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "151")
public class HellionEruption extends Card {

    public HellionEruption() {
        var creature = new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new PermanentCount(creature, CountScope.CONTROLLER), creature, SacrificeRecipient.CONTROLLER)
                .withRecordedSacrificeCount());
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new EventValue(), "Hellion", 4, 4,
                CardColor.RED, List.of(CardSubtype.HELLION), Set.of(), Set.of()));
    }
}
