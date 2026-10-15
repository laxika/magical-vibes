package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "30")
public class EchoingCavern extends Card {

    public EchoingCavern() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add one mana of any color. Spend this mana only to cast spells of the chosen type.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CHOSEN_SUBTYPE_SPELL)),
                "{T}: Add one mana of any color. Spend this mana only to cast spells of the chosen type."
        ));
        // Exhaust — {4}, {T}: Seek a card of the chosen type.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SeekLibraryEffect(1, new CardHasSourceChosenSubtypePredicate(false))),
                "Exhaust — {4}, {T}: Seek a card of the chosen type."
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
