package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "1")
public class KarnLivingLegacy extends Card {

    public KarnLivingLegacy() {
        // +1: Create a tapped Powerstone token.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(CreateTokenEffect.ofPowerstoneToken(new Fixed(1))),
                "+1: Create a tapped Powerstone token."
        ));

        // −1: Pay any amount of mana. Look at that many cards from the top of your library, then
        // put one of those cards into your hand and the rest on the bottom of your library in a
        // random order.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(
                        new PayXManaEffect(),
                        LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(new EventValue(), 1)
                ),
                "−1: Pay any amount of mana. Look at that many cards from the top of your library, "
                        + "then put one of those cards into your hand and the rest on the bottom of "
                        + "your library in a random order."
        ));

        // −7: You get an emblem with "Tap an untapped artifact you control: This emblem deals 1
        // damage to any target."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new DealDamageToAnyTargetEffect(1)),
                                        "{T}: This emblem deals 1 damage to any target."
                                ),
                                GrantScope.OWN_PERMANENTS,
                                new PermanentIsArtifactPredicate()
                        )),
                        "Tap an untapped artifact you control: This emblem deals 1 damage to any target."
                )),
                "−7: You get an emblem with \"Tap an untapped artifact you control: This emblem deals "
                        + "1 damage to any target.\""
        ));
    }
}
