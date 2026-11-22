package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "174")
@CardRegistration(set = "WOC", collectorNumber = "7")
@CardRegistration(set = "WOC", collectorNumber = "43")
public class SongbirdsBlessing extends Card {

    public SongbirdsBlessing() {
        var aura = new CardIsAuraPredicate();
        // The attack trigger is the Aura's own ability, so its controller (whose library is revealed) is the
        // Aura's controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature());
        addTrigger(Triggers.attacks(),
                new RevealCardsFromTopUntilEffect(aura),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, aura),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(aura)),
                        "Put the revealed Aura onto the battlefield?")),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay(aura)),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }
}
