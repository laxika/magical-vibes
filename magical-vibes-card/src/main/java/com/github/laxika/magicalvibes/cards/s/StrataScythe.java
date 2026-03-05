package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreaturePerMatchingLandNameEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "206")
public class StrataScythe extends Card {

    public StrataScythe() {
        // Imprint — When Strata Scythe enters the battlefield, search your library
        // for a land card, exile it, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForCardTypeToExileAndImprintEffect(Set.of(CardType.LAND)));

        // Equipped creature gets +1/+1 for each land on the battlefield with the
        // same name as the exiled card.
        addEffect(EffectSlot.STATIC,
                new BoostAttachedCreaturePerMatchingLandNameEffect(1, 1));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
