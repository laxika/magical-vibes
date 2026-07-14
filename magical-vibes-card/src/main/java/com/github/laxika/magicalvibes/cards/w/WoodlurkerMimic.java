package com.github.laxika.magicalvibes.cards.w;

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

@CardRegistration(set = "EVE", collectorNumber = "130")
public class WoodlurkerMimic extends Card {

    public WoodlurkerMimic() {
        // Whenever you cast a spell that's both black and green, this creature has base power and
        // toughness 4/5 until end of turn and gains wither until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.BLACK),
                        new CardColorPredicate(CardColor.GREEN))),
                List.of(
                        new SetBasePowerToughnessEffect(4, 5, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.WITHER, GrantScope.SELF))));
    }
}
