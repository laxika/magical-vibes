package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "39")
public class WingmantleChaplain extends Card {

    public WingmantleChaplain() {
        PermanentCount defendersYouControl = new PermanentCount(
                new PermanentHasKeywordPredicate(Keyword.DEFENDER), CountScope.CONTROLLER);
        CreateTokenEffect birdToken = new CreateTokenEffect(
                "Bird", 1, 1, CardColor.WHITE, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                defendersYouControl, "Bird", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEFENDER), birdToken));
    }
}
