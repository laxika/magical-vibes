package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EXO", collectorNumber = "94")
public class Paroxysm extends Card {

    public Paroxysm() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(LibraryOwner.ENCHANTED_PERMANENT_CONTROLLER),
                        new ConditionalReplacementEffect(
                                new TopCardOfLibraryType(CardType.LAND, LibraryOwner.ENCHANTED_PERMANENT_CONTROLLER),
                                new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(3), new Fixed(3)),
                                new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED))));
    }
}
