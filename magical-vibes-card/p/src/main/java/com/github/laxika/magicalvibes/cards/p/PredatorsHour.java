package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "147")
@CardRegistration(set = "VOC", collectorNumber = "21")
@CardRegistration(set = "VOC", collectorNumber = "59")
public class PredatorsHour extends Card {

    public PredatorsHour() {
        addSpellEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES));
        addSpellEffect(new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect())));
    }
}
