package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageBySelfToCreaturesAndShuffleEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "168")
@CardRegistration(set = "WHO", collectorNumber = "453")
@CardRegistration(set = "WHO", collectorNumber = "549")
@CardRegistration(set = "WHO", collectorNumber = "773")
@CardRegistration(set = "WHO", collectorNumber = "1044")
@CardRegistration(set = "WHO", collectorNumber = "1140")
public class WeepingAngel extends Card {

    public WeepingAngel() {
        // Whenever an opponent casts a creature spell, Weeping Angel isn't a creature until end of turn.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new SetCardTypesUntilEndOfTurnEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF))
        ));

        // If Weeping Angel would deal combat damage to a creature, prevent that damage and shuffle
        // that creature into its owner's library.
        addEffect(EffectSlot.STATIC, new PreventCombatDamageBySelfToCreaturesAndShuffleEffect());
    }
}
