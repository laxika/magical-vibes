package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "10")
@CardRegistration(set = "BLC", collectorNumber = "46")
public class Murmuration extends Card {

    public Murmuration() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, Set.of(Keyword.VIGILANCE), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.BIRD)));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new CreateTokenEffect(
                        new SpellsCastThisTurn(CountScope.CONTROLLER), "Storm Crow", 1, 2,
                        CardColor.BLUE, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
    }
}
