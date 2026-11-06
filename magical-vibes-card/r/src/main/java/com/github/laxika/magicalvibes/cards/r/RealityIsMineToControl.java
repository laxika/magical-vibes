package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;

@CardRegistration(set = "DSC", collectorNumber = "354")
public class RealityIsMineToControl extends Card {

    public RealityIsMineToControl() {
        addTrigger(Triggers.youCastSpell().anySpell(), new MayEffect(
                new SacrificePermanentThenEffect(new PermanentIsSourcePermanentPredicate(),
                        new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens(), "this scheme", false, false),
                "Abandon this scheme and copy that spell?"));
    }
}
