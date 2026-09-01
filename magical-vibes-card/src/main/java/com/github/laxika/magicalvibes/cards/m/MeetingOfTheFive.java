package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "202")
public class MeetingOfTheFive extends Card {

    public MeetingOfTheFive() {
        CardAllOfPredicate exactlyThreeColorSpell = new CardAllOfPredicate(List.of(
                new CardHasExactlyNColorsPredicate(3),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.SPELL,
                new ExileTopCardsMayCastMatchingThisTurnEffect(10, exactlyThreeColorSpell));

        ManaRestriction restriction = new ManaRestriction.ExactlyThreeColorSpells();
        addEffect(EffectSlot.SPELL, new AwardRestrictedManaEffect(ManaColor.WHITE, 2, restriction));
        addEffect(EffectSlot.SPELL, new AwardRestrictedManaEffect(ManaColor.BLUE, 2, restriction));
        addEffect(EffectSlot.SPELL, new AwardRestrictedManaEffect(ManaColor.BLACK, 2, restriction));
        addEffect(EffectSlot.SPELL, new AwardRestrictedManaEffect(ManaColor.RED, 2, restriction));
        addEffect(EffectSlot.SPELL, new AwardRestrictedManaEffect(ManaColor.GREEN, 2, restriction));
    }
}
