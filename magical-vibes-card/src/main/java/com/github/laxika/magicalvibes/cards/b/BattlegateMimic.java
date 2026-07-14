package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "133")
public class BattlegateMimic extends Card {

    public BattlegateMimic() {
        // Whenever you cast a spell that's both red and white, this creature has base power and
        // toughness 4/2 until end of turn and gains first strike until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.RED),
                        new CardColorPredicate(CardColor.WHITE))),
                List.of(
                        new SetBasePowerToughnessEffect(4, 2, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF))));
    }
}
