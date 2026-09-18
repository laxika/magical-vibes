package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "50")
public class FoundingTheThirdPath extends Card {

    public FoundingTheThirdPath() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        // Chapter I: You may cast an instant or sorcery spell with mana value 1 or 2
        // from your hand without paying its mana cost.
        CardPredicate lowManaInstantOrSorcery = new CardAllOfPredicate(List.of(
                instantOrSorcery, new CardMaxManaValuePredicate(2)));
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(lowManaInstantOrSorcery));

        // Chapter II: Target player mills four cards.
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MillEffect(4, MillRecipient.TARGET_PLAYER));

        // Chapter III: Exile target instant or sorcery card from your graveyard. Copy it.
        // You may cast the copy.
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false));
    }
}
