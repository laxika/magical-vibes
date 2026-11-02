package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "82")
public class PuppetConjurer extends Card {

    public PuppetConjurer() {
        // {U}, {T}: Create a 0/1 blue Homunculus artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new CreateTokenEffect(
                        "Homunculus", 0, 1, CardColor.BLUE,
                        List.of(CardSubtype.HOMUNCULUS),
                        Set.of(), Set.of(CardType.ARTIFACT))),
                "{U}, {T}: Create a 0/1 blue Homunculus artifact creature token."
        ));

        // At the beginning of your upkeep, sacrifice a Homunculus.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificePermanentsEffect(
                1,
                new PermanentHasSubtypePredicate(CardSubtype.HOMUNCULUS),
                SacrificeRecipient.CONTROLLER));
    }
}
