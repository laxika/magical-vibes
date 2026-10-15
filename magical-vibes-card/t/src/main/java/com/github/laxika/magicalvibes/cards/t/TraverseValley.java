package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "18")
public class TraverseValley extends Card {

    public TraverseValley() {
        CardAllOfPredicate nonbasicLand = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.BASIC))
        ));

        addEffect(EffectSlot.STATIC, KickerEffect.forage());
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new SeekLibraryEffect(1, nonbasicLand, LibrarySearchDestination.HAND),
                new SeekLibraryEffect(1, nonbasicLand, LibrarySearchDestination.BATTLEFIELD_TAPPED)
        ));
    }
}
