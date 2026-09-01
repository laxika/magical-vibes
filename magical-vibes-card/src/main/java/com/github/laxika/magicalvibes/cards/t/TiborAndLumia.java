package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "135")
public class TiborAndLumia extends Card {

    public TiborAndLumia() {
        // Whenever you cast a blue spell, target creature gains flying until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.BLUE),
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                null,
                TargetFilters.creature()
        ));

        // Whenever you cast a red spell, Tibor and Lumia deals 1 damage to each creature without flying.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new MassDamageEffect(1, false, false,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
                )
        ));
    }
}
