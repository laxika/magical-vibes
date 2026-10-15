package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "12")
public class EmeriasCall extends Card {

    public EmeriasCall() {
        EmeriaShatteredSkyclave backFace = new EmeriaShatteredSkyclave();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Emeria's Call", List.of(
                        new CreateTokenEffect(
                                2, "Angel Warrior", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR),
                                Set.of(Keyword.FLYING), Set.of()),
                        new GrantKeywordEffect(
                                Set.of(Keyword.INDESTRUCTIBLE),
                                GrantScope.OWN_CREATURES,
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ANGEL)),
                                GrantDuration.UNTIL_YOUR_NEXT_TURN,
                                null
                        )
                )).withManaCost("{4}{W}{W}{W}"),
                new ChooseOneEffect.ChooseOneOption("Emeria, Shattered Skyclave", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "EmeriaShatteredSkyclave";
    }
}
