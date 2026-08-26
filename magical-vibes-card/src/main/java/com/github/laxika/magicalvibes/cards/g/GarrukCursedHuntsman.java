package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "191")
public class GarrukCursedHuntsman extends Card {

    private static final String EMBLEM_TEXT = "Creatures you control get +3/+3 and have trample.";

    public GarrukCursedHuntsman() {
        // 0: Create two 2/2 black and green Wolf creature tokens with "When this token dies, put
        // a loyalty counter on each Garruk you control."
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 2, "Wolf", 2, 2, CardColor.BLACK,
                        Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.WOLF), Set.of(), Set.of(),
                        false, false,
                        Map.of(EffectSlot.ON_DEATH, new PutCounterOnEachControlledPermanentEffect(
                                CounterType.LOYALTY, 1, new PermanentHasSubtypePredicate(CardSubtype.GARRUK))),
                        List.of(), false, false, false, 0, Set.of()
                )),
                "0: Create two 2/2 black and green Wolf creature tokens with \"When this token dies, "
                        + "put a loyalty counter on each Garruk you control.\""
        ));

        // −3: Destroy target creature. Draw a card.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect(), new DrawCardEffect(1)),
                "−3: Destroy target creature. Draw a card.",
                TargetFilters.creature()
        ));

        // −6: You get an emblem with "Creatures you control get +3/+3 and have trample."
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new StaticBoostEffect(3, 3, GrantScope.OWN_CREATURES),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                        ),
                        EMBLEM_TEXT
                )),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
