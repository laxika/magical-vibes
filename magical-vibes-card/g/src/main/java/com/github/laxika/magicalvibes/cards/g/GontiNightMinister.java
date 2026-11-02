package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "87")
public class GontiNightMinister extends Card {

    public GontiNightMinister() {
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardControllerDoesNotOwnPredicate(),
                List.of(new CreateTokenForTriggeringPlayerEffect(CreateTokenEffect.ofTreasureToken(1)))
        ));
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(),
                new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect());
    }
}
