package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "108")
@CardRegistration(set = "C15", collectorNumber = "234")
@CardRegistration(set = "C21", collectorNumber = "230")
public class TeysaEnvoyOfGhosts extends Card {

    public TeysaEnvoyOfGhosts() {
        addStaticEffect(new ProtectionFromCardTypesEffect(Set.of(CardType.CREATURE)));

        // Whenever a creature deals combat damage to you, destroy that creature. Create a 1/1
        // white and black Spirit creature token with flying. The trigger's stack entry binds the
        // damaging creature as its (untargeted) subject, so DestroyTargetPermanentEffect resolves
        // as "that creature".
        addTrigger(Triggers.creatureDealsCombatDamageToYou(), new DestroyTargetPermanentEffect(),
                new CreateTokenEffect(
                1, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
