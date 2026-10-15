package com.github.laxika.magicalvibes.cards.n;

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

@CardRegistration(set = "EVE", collectorNumber = "91")
public class NightskyMimic extends Card {

    public NightskyMimic() {
        // Whenever you cast a spell that's both white and black, this creature has base power and
        // toughness 4/4 until end of turn and gains flying until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardColorPredicate(CardColor.BLACK))),
                List.of(
                        new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF))));
    }
}
