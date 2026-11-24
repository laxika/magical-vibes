package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RollTwoDiceChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastAmountPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "13")
public class ValiantEndeavor extends Card {

    public ValiantEndeavor() {
        addSpellEffect(new RollTwoDiceChooseOneEffect(6));
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastAmountPredicate(new XValue())))));
        addSpellEffect(new CreateTokenEffect(new EventValue(), "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of()));
    }
}
