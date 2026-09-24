package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "22")
@CardRegistration(set = "SOC", collectorNumber = "72")
public class OwlinSpiralmancer extends Card {

    public OwlinSpiralmancer() {
        CardHasXInManaCostPredicate xSpell = new CardHasXInManaCostPredicate();
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new CopyControllerCastSpellOnSpellCastEffect(
                        xSpell, null, null, null, null, Set.of(), null, false, Set.of(),
                        true, true, false, false, null, List.of(xSpell), null),
                "Copy that spell?"));
    }
}
