package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CompletedDungeonsCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "181")
public class EllywickTumblestrum extends Card {

    private static final String EMBLEM_TEXT =
            "Creatures you control have trample and haste and get +2/+2 for each differently named dungeon you've completed.";

    public EllywickTumblestrum() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new VentureIntoDungeonEffect()),
                "+1: Venture into the dungeon."));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        6,
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new GainLifeEffect(3))),
                "−2: Look at the top six cards of your library. You may reveal a creature card from among them and put it into your hand. If it's legendary, you gain 3 life. Put the rest on the bottom of your library in a random order."));

        CompletedDungeonsCount completedDungeons = new CompletedDungeonsCount();
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(List.of(
                        new StaticBoostEffect(0, 0, Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                                GrantScope.OWN_CREATURES),
                        new DynamicStaticBoostEffect(
                                new Scaled(completedDungeons, 2),
                                new Scaled(completedDungeons, 2),
                                GrantScope.OWN_CREATURES)), EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\"."));
    }
}
