package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.LandsMatchingImprintedName;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "206")
@CardRegistration(set = "C14", collectorNumber = "274")
public class StrataScythe extends Card {

    public StrataScythe() {
        // Imprint — When Strata Scythe enters the battlefield, search your library
        // for a land card, exile it, then shuffle.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForCardTypeToExileAndImprintEffect(Set.of(CardType.LAND)));

        // Equipped creature gets +1/+1 for each land on the battlefield with the
        // same name as the exiled card.
        addStaticEffect(new AttachedBoostEffect(
                new LandsMatchingImprintedName(),
                new LandsMatchingImprintedName(),
                GrantScope.EQUIPPED_CREATURE));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
