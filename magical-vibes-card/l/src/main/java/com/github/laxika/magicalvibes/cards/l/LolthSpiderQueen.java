package com.github.laxika.magicalvibes.cards.l;

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
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToThresholdOnCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "112")
public class LolthSpiderQueen extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever an opponent is dealt combat damage by one or more creatures you control, if that player lost less than 8 life this turn, they lose life equal to the difference.";

    public LolthSpiderQueen() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new PutCountersOnSourceCardEffect(CounterType.LOYALTY));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect(1), new LoseLifeEffect(1)),
                "0: You draw a card and you lose 1 life."));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect(
                        2, "Spider", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH, Keyword.MENACE), Set.of())),
                "−3: Create two 2/1 black Spider creature tokens with reach and menace."));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new LoseLifeToThresholdOnCombatDamageEffect(8)), EMBLEM_TEXT)),
                "−8: You get an emblem with \"" + EMBLEM_TEXT + "\"."));
    }
}
