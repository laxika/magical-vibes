package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "32")
@CardRegistration(set = "TDC", collectorNumber = "72")
public class WithinRange extends Card {

    public WithinRange() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.RED,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of()));
        addTrigger(Triggers.youAttackPlayer(),
                new LoseLifeEffect(new PermanentCount(new PermanentIsAttackingDefendingPlayerPredicate(), CountScope.ANY_PLAYER),
                        LoseLifeRecipient.DEFENDING_PLAYER));
    }
}
