package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "35")
@CardRegistration(set = "OTC", collectorNumber = "71")
public class VengefulRegrowth extends Card {

    public VengefulRegrowth() {
        CardPredicate land = new CardTypePredicate(CardType.LAND);
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                land, 3, false, true));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new EventValue(), "Plant Warrior", 4, 2, CardColor.GREEN,
                List.of(CardSubtype.PLANT, CardSubtype.WARRIOR),
                Set.of(Keyword.REACH), Set.of()));
        addCastingOption(new FlashbackCast("{6}{G}{G}"));
    }
}
