package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromLibraryTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "38")
@CardRegistration(set = "TDC", collectorNumber = "78")
public class ThundermaneDragon extends Card {

    public ThundermaneDragon() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardPowerAtLeastPredicate(4)
        ))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CastFromLibraryTriggerEffect(List.of(
                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE))
        )));
    }
}
