package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "3")
public class AngelicDestiny extends Card {

    public AngelicDestiny() {
        target(TargetFilters.creature())
                // Enchanted creature gets +4/+4 and has flying and first strike.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        4, 4, Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE))
                // ... and is an Angel in addition to its other types.
                .addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.ANGEL, GrantScope.ENCHANTED_CREATURE))
                // When enchanted creature dies, return this card to its owner's hand.
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
