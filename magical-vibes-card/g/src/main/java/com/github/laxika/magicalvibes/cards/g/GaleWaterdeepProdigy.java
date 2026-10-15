package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2488")
public class GaleWaterdeepProdigy extends Card {

    public GaleWaterdeepProdigy() {
        // Whenever you cast an instant or sorcery spell from your hand, you may cast up to one target
        // card of the other type from your graveyard. If a spell cast from your graveyard this way
        // would be put into your graveyard, exile it instead.
        addCastOtherTypeTrigger(CardType.INSTANT, CardType.SORCERY);
        addCastOtherTypeTrigger(CardType.SORCERY, CardType.INSTANT);
    }

    private void addCastOtherTypeTrigger(CardType castType, CardType graveyardType) {
        CardTypePredicate graveyardFilter = new CardTypePredicate(graveyardType);
        CastTargetInstantOrSorceryFromGraveyardEffect castFromGraveyard =
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, true, graveyardFilter);
        target(new GraveyardCardPredicateTargetFilter(graveyardFilter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                0, 1)
                .bindEffect(castFromGraveyard);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastFromHandTriggerEffect(
                new CardTypePredicate(castType), List.of(castFromGraveyard)));
    }
}
