package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardOfDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OTC", collectorNumber = "22")
@CardRegistration(set = "OTC", collectorNumber = "58")
public class OrochiSoulReaver extends Card {

    public OrochiSoulReaver() {
        addNinjutsu("{3}{B}");
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, SequenceEffect.of(
                        CreateTokenEffect.ofTreasureToken(1),
                        new ManifestTopCardOfDamagedPlayerLibraryEffect()), false, true));
    }
}
