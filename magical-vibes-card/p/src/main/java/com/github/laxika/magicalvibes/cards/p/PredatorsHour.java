package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "OTC", collectorNumber = "147")
@CardRegistration(set = "VOC", collectorNumber = "21")
@CardRegistration(set = "VOC", collectorNumber = "59")
public class PredatorsHour extends Card {

    public PredatorsHour() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SPELL, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect()));
    }
}
