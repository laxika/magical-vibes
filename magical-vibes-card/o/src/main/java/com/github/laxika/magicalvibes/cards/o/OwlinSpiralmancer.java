package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "22")
@CardRegistration(set = "SOC", collectorNumber = "72")
public class OwlinSpiralmancer extends Card {

    public OwlinSpiralmancer() {
        CardHasXInManaCostPredicate xSpell = new CardHasXInManaCostPredicate();
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new CopyControllerCastSpellOnSpellCastEffect(
                        xSpell, null, null, null, null, Set.of(), null, false, Set.of(),
                        true, true, false, false, null, List.of(xSpell), null),
                "Copy that spell?"));
    }
}
