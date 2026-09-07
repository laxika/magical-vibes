package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class CaetusSeaTyrantOfSegovia extends Card {

    public CaetusSeaTyrantOfSegovia() {
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        target(TargetFilters.creature(), 0, 4)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS));
    }
}
