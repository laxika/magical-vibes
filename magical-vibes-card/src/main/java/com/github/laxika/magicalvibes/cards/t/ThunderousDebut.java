package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "190")
public class ThunderousDebut extends Card {

    public ThunderousDebut() {
        addEffect(EffectSlot.STATIC, new KickerEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentIsTokenPredicate()
                )),
                "an artifact, enchantment, or token"
        ));

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        LookAtTopCardsEffect toHand = new LookAtTopCardsEffect(
                new Fixed(20), new Fixed(2), creature,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.HAND, true);
        LookAtTopCardsEffect toBattlefield = new LookAtTopCardsEffect(
                new Fixed(20), new Fixed(2), creature,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.BATTLEFIELD, true);
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(), toHand, toBattlefield));
        addEffect(EffectSlot.SPELL, new ShuffleLibraryEffect(false));
    }
}
