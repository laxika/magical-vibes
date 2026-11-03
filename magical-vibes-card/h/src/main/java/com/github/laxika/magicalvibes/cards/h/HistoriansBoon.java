package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "21")
@CardRegistration(set = "DMC", collectorNumber = "71")
public class HistoriansBoon extends Card {

    public HistoriansBoon() {
        CreateTokenEffect soldier = CreateTokenEffect.whiteSoldier(1);
        addTrigger(Triggers.enters(ObjectRelation.THIS), soldier);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ENCHANTMENT,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                soldier);
        addTrigger(Triggers.finalChapterAbilityTriggers(),
                new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of()));
    }
}
