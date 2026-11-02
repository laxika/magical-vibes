package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrainLifeFromDyingCreatureOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardOfDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "150")
@CardRegistration(set = "C19", collectorNumber = "21")
public class ThievingAmalgam extends Card {

    public ThievingAmalgam() {
        // Triggers.upkeep(ActivePlayer.OPPONENT) supplies the active opponent as targetId; the manifest
        // handler uses that player's library and puts the manifested card under this source's control.
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new ManifestTopCardOfDamagedPlayerLibraryEffect());

        // "A creature you control but don't own" includes this card itself while an opponent owns it
        // (it can be controlled by another player's Control Magic effect).
        var drainOwner = new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()),
                new DrainLifeFromDyingCreatureOwnerEffect(2));
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), drainOwner);
    }
}
