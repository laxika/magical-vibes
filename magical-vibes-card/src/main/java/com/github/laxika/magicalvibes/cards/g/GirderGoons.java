package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "80")
public class GirderGoons extends Card {

    public GirderGoons() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{B}"))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForAlternateCost(),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForAlternateCost(),
                new SacrificeSelfAtEndStepEffect()));
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Rogue", 2, 2, CardColor.BLACK, List.of(CardSubtype.ROGUE),
                Set.<Keyword>of(), Set.<CardType>of(), true));
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new CastForAlternateCost(), new DrawCardEffect(1)));
    }
}
