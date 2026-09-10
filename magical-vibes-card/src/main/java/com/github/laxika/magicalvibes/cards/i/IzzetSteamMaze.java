package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "19")
public class IzzetSteamMaze extends Card {

    public IzzetSteamMaze() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                SpellCastTriggerEffect.anyPlayer(instantOrSorcery, List.of(new CopyTriggeringSpellEffect())));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(instantOrSorcery, 3));
    }
}
