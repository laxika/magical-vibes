package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "461")
public class FishingGear extends Card {

    public FishingGear() {
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                new ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect(
                        new CreateTokenEffect("Fish", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.FISH), Set.of(), Set.of())));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
