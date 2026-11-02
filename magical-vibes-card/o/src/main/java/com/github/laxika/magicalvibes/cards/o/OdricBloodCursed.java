package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DistinctKeywordAbilitiesAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "243")
@CardRegistration(set = "DBL", collectorNumber = "510")
public class OdricBloodCursed extends Card {

    private static final Set<Keyword> COUNTED_ABILITIES = Set.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public OdricBloodCursed() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                CreateTokenEffect.ofBloodToken(
                        new DistinctKeywordAbilitiesAmongControlledCreatures(COUNTED_ABILITIES)));
    }
}
