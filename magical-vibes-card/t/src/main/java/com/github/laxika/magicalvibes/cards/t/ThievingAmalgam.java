package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrainLifeFromDyingCreatureOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardOfDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

@CardRegistration(set = "OTC", collectorNumber = "150")
@CardRegistration(set = "C19", collectorNumber = "21")
public class ThievingAmalgam extends Card {

    public ThievingAmalgam() {
        // OPPONENT_UPKEEP_TRIGGERED supplies the active opponent as targetId; the manifest
        // handler uses that player's library and puts the manifested card under this source's control.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new ManifestTopCardOfDamagedPlayerLibraryEffect());

        // "A creature you control but don't own" includes this card itself while an opponent owns it
        // (it can be controlled by another player's Control Magic effect), and ON_ALLY_CREATURE_DIES
        // never looks at the watcher's own death, so that case is authored on ON_DEATH.
        var drainOwner = new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()),
                new DrainLifeFromDyingCreatureOwnerEffect(2));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, drainOwner);
        addEffect(EffectSlot.ON_DEATH, drainOwner);
    }
}
