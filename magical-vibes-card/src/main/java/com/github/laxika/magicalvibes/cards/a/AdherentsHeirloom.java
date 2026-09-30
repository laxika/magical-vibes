package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.NoteMostPrevalentCreatureTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "29")
public class AdherentsHeirloom extends Card {

    public AdherentsHeirloom() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new NoteMostPrevalentCreatureTypeOnEnterEffect(false));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SeekLibraryEffect(1, new CardHasSourceChosenSubtypePredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CREATURE_SPELL_ONLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast a creature spell."
        ));
    }
}
