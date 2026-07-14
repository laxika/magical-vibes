package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "139")
public class FigureOfDestiny extends Card {

    public FigureOfDestiny() {
        // While it's an Avatar (permanently granted by the third ability) it has flying and first strike.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasSubtype(CardSubtype.AVATAR),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasSubtype(CardSubtype.AVATAR),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));

        // {R/W}: This creature becomes a Kithkin Spirit with base power and toughness 2/2.
        addActivatedAbility(new ActivatedAbility(false, "{R/W}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(2, 2, CardSubtype.SPIRIT)),
                "{R/W}: This creature becomes a Kithkin Spirit with base power and toughness 2/2."));

        // {R/W}{R/W}{R/W}: If this creature is a Spirit, it becomes a Kithkin Spirit Warrior with base power and toughness 4/4.
        addActivatedAbility(new ActivatedAbility(false, "{R/W}{R/W}{R/W}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(4, 4, CardSubtype.WARRIOR, CardSubtype.SPIRIT)),
                "{R/W}{R/W}{R/W}: If this creature is a Spirit, it becomes a Kithkin Spirit Warrior with base power and toughness 4/4."));

        // {R/W}{R/W}{R/W}{R/W}{R/W}{R/W}: If this creature is a Warrior, it becomes a Kithkin Spirit Warrior Avatar
        // with base power and toughness 8/8, flying, and first strike.
        addActivatedAbility(new ActivatedAbility(false, "{R/W}{R/W}{R/W}{R/W}{R/W}{R/W}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(8, 8, CardSubtype.AVATAR, CardSubtype.WARRIOR)),
                "{R/W}{R/W}{R/W}{R/W}{R/W}{R/W}: If this creature is a Warrior, it becomes a Kithkin Spirit Warrior Avatar with base power and toughness 8/8, flying, and first strike."));
    }
}
