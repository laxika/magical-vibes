package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "15")
public class ArchaeomancersSpade extends Card {

    public ArchaeomancersSpade() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SequenceEffect(List.of(
                new SeekFromLibraryToGraveyardEffect(new CardHasFlashbackPredicate()),
                new SeekFromLibraryToGraveyardEffect(new CardHasFlashbackPredicate()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.RED, 1, new ManaRestriction.NonHandSpells()),
                        new AwardRestrictedManaEffect(ManaColor.WHITE, 1, new ManaRestriction.NonHandSpells())),
                "{T}: Add {R}{W}. This mana can't be spent to cast spells from your hand."
        ));
    }
}
