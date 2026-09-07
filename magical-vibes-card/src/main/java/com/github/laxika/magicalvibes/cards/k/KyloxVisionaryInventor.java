package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "214")
@CardRegistration(set = "MKM", collectorNumber = "365")
public class KyloxVisionaryInventor extends Card {

    public KyloxVisionaryInventor() {
        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new SacrificeAnyNumberOfPermanentsEffect(otherCreature, true),
                new ExileTopCardsAndMayCastSpellsEffect(
                        new SacrificedPermanentPower(), LibraryScope.CONTROLLER, false, null,
                        instantOrSorcery)));
    }
}
