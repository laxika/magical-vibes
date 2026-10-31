package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreaturesBlockingEffect;

@CardRegistration(set = "C15", collectorNumber = "13")
public class MirrorMatch extends Card {

    public MirrorMatch() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_BLOCKERS);
        addSpellEffect(new CreateTokenCopyOfAttackingCreaturesBlockingEffect());
    }
}
