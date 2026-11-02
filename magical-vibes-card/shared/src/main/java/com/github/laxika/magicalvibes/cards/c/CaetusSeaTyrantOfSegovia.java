package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class CaetusSeaTyrantOfSegovia extends Card {

    public CaetusSeaTyrantOfSegovia() {
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        target(TargetFilters.creature(), 0, 4)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS));
    }
}
