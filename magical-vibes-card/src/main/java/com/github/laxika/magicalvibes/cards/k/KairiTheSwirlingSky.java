package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsWithinTotalManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "60")
public class KairiTheSwirlingSky extends Card {

    public KairiTheSwirlingSky() {
        PermanentPredicate nonlandPermanent = TargetFilters.nonlandPermanent().predicate();
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        ReturnTargetPermanentsWithinTotalManaValueEffect returnPermanents =
                ReturnTargetPermanentsWithinTotalManaValueEffect.withinTotalManaValue(
                        nonlandPermanent, 6);

        addEffect(EffectSlot.ON_DEATH, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return any number of target nonland permanents with total mana value 6 or less to their owners' hands",
                        List.of(returnPermanents), TargetFilters.nonlandPermanent(), null, 0, 99, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Mill six cards, then return up to two instant and/or sorcery cards from your graveyard to your hand",
                        List.of(
                                new MillEffect(6, MillRecipient.CONTROLLER),
                                new ReturnCardsFromControllerGraveyardToHandEffect(
                                        instantOrSorcery, new Fixed(2)))))));
    }
}
