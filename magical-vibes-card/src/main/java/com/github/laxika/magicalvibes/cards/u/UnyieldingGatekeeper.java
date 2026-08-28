package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "35")
@CardRegistration(set = "MKM", collectorNumber = "392")
public class UnyieldingGatekeeper extends Card {

    public UnyieldingGatekeeper() {
        addMorph("{1}{W}");
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "Target must be another nonland permanent"))
                .addEffect(EffectSlot.ON_TURNED_FACE_UP,
                        new ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffect(
                                new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                                        Set.of(CardColor.WHITE, CardColor.BLUE),
                                        List.of(CardSubtype.DETECTIVE))));
    }
}
