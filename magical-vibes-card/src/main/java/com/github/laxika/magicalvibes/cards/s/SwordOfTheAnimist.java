package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ORI", collectorNumber = "240")
public class SwordOfTheAnimist extends Card {

    public SwordOfTheAnimist() {
        // Equipped creature gets +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature attacks, you may search your library for a basic land card,
        // put it onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "Search your library for a basic land card?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
