package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "236")
public class ArgothianWurm extends Card {

    public ArgothianWurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.acceptedPlayers()),
                "Sacrifice a land? If a player does, it is put on top of its owner's library.",
                PutTargetOnTopOfLibraryEffect.self(), null));
    }
}
