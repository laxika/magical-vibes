package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ACR", collectorNumber = "21")
public class LoyalInventor extends Card {

    public LoyalInventor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                SequenceEffect.of(
                        ConditionalEffect.unless(
                                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)),
                                new SearchLibraryEffect(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        LibrarySearchDestination.HAND)),
                        ConditionalEffect.unless(
                                new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN))),
                                new SearchLibraryEffect(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        LibrarySearchDestination.TOP_OF_LIBRARY))),
                "Search your library for an artifact card?"));
    }
}
