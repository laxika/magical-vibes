package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "13")
public class StormcallersBoon extends Card {

    public StormcallersBoon() {
        // Sacrifice this enchantment: Creatures you control gain flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES)),
                "Sacrifice this enchantment: Creatures you control gain flying until end of turn."
        ));

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
