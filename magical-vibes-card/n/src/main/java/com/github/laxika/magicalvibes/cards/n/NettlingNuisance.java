package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GoadSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "15")
@CardRegistration(set = "WOC", collectorNumber = "51")
public class NettlingNuisance extends Card {

    public NettlingNuisance() {
        CreateTokenEffect pirateToken = new CreateTokenEffect(
                1,
                "Pirate",
                4,
                2,
                CardColor.RED,
                List.of(CardSubtype.PIRATE),
                Set.of(),
                Set.of(),
                List.of(
                        StaticAbility.of(SequenceEffect.of(
                        new CantBlockEffect(),
                        new GoadSourceCreatureEffect()))));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.FAERIE)),
                TriggerOptions.ONE_OR_MORE,
                new CreateTokenForTargetPlayerEffect(pirateToken));
    }
}
