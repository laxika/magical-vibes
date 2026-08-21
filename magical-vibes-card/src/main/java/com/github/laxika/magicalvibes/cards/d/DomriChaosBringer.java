package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "166")
public class DomriChaosBringer extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of each end step, create a 4/4 red and green Beast creature token with trample.";

    public DomriChaosBringer() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN), true)),
                "+1: Add {R} or {G}. If that mana is spent on a creature spell, it gains riot."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new LookAtTopCardsEffect(
                        new Fixed(4), new Fixed(2), new CardTypePredicate(CardType.CREATURE),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                        LibrarySearchDestination.HAND, true)),
                "−3: Look at the top four cards of your library. You may reveal up to two creature cards "
                        + "from among them and put them into your hand. Put the rest on the bottom of your "
                        + "library in a random order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.END_STEP,
                                List.of(new CreateTokenEffect(
                                        CardType.CREATURE, 1, "Beast", 4, 4, CardColor.RED,
                                        Set.of(CardColor.RED, CardColor.GREEN), List.of(CardSubtype.BEAST),
                                        Set.of(Keyword.TRAMPLE), Set.of(), false, false, java.util.Map.of(),
                                        List.of(), false, false, false, 0, Set.of())),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−8: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
