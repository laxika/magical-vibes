package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "68")
public class Spelltwine extends Card {

    public Spelltwine() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));

        // Exile target instant or sorcery card from your graveyard and target instant or sorcery card
        // from an opponent's graveyard. Copy those cards. Cast the copies if able without paying their
        // mana costs. The effect stays unbound so it applies to both declared targets.
        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, GraveyardSearchScope.OPPONENT_GRAVEYARD));
        addEffect(EffectSlot.SPELL, new ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect());

        // Exile Spelltwine.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
