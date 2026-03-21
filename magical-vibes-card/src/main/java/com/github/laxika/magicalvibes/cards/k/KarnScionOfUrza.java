package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KarnScionReturnSilverCounterCardEffect;
import com.github.laxika.magicalvibes.model.effect.KarnScionRevealTwoOpponentChoosesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "1")
public class KarnScionOfUrza extends Card {

    public KarnScionOfUrza() {
        // +1: Reveal the top two cards of your library. An opponent chooses one of them.
        // Put that card into your hand and exile the other with a silver counter on it.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new KarnScionRevealTwoOpponentChoosesEffect()),
                "+1: Reveal the top two cards of your library. An opponent chooses one of them. Put that card into your hand and exile the other with a silver counter on it."
        ));

        // −1: Put a card you own with a silver counter on it from exile into your hand.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new KarnScionReturnSilverCounterCardEffect()),
                "\u22121: Put a card you own with a silver counter on it from exile into your hand."
        ));

        // −2: Create a 0/0 colorless Construct artifact creature token with
        // "This creature gets +1/+1 for each artifact you control."
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateCreatureTokenEffect(
                        1, "Construct", 0, 0,
                        null, List.of(CardSubtype.CONSTRUCT),
                        Set.of(), Set.of(CardType.ARTIFACT),
                        Map.of(EffectSlot.STATIC,
                                new BoostSelfPerControlledPermanentEffect(1, 1,
                                        new PermanentIsArtifactPredicate()))
                )),
                "\u22122: Create a 0/0 colorless Construct artifact creature token with \"This creature gets +1/+1 for each artifact you control.\""
        ));
    }
}
