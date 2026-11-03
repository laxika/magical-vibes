package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardOfDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "22")
@CardRegistration(set = "OTC", collectorNumber = "58")
public class OrochiSoulReaver extends Card {

    public OrochiSoulReaver() {
        addNinjutsu("{3}{B}");
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        CreateTokenEffect.ofTreasureToken(1),
                        new ManifestTopCardOfDamagedPlayerLibraryEffect()));
    }
}
