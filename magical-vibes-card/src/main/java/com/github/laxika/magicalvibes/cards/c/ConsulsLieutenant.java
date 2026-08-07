package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "11")
public class ConsulsLieutenant extends Card {

    public ConsulsLieutenant() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));

        // Whenever Consul's Lieutenant attacks, if it's renowned, other attacking creatures you
        // control get +1/+1 until end of turn.
        var otherAttackers = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceIsRenowned(), new BoostAllOwnCreaturesEffect(1, 1, otherAttackers)));
    }
}
