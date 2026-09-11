package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DistinctKeywordAbilitiesAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "243")
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
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CreateTokenEffect.ofBloodToken(
                        new DistinctKeywordAbilitiesAmongControlledCreatures(COUNTED_ABILITIES)));
    }
}
