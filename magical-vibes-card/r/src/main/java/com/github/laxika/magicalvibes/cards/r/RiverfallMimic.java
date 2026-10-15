package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "111")
public class RiverfallMimic extends Card {

    public RiverfallMimic() {
        // Whenever you cast a spell that's both blue and red, this creature has base
        // power and toughness 3/3 until end of turn and can't be blocked this turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.BLUE),
                        new CardColorPredicate(CardColor.RED)
                )),
                List.of(
                        new SetBasePowerToughnessEffect(3, 3, GrantScope.SELF),
                        new MakeCreatureUnblockableEffect(true)
                )
        ));
    }
}
