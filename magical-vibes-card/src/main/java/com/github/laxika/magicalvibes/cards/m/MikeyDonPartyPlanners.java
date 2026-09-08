package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "157")
@CardRegistration(set = "TMT", collectorNumber = "246")
public class MikeyDonPartyPlanners extends Card {

    public MikeyDonPartyPlanners() {
        CardPredicate partySubtype = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.MUTANT),
                new CardSubtypePredicate(CardSubtype.NINJA),
                new CardSubtypePredicate(CardSubtype.TURTLE)));
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new PlayLandsFromTopOfLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(
                java.util.Set.of(), false, partySubtype, false, List.of(), 1));
    }
}
