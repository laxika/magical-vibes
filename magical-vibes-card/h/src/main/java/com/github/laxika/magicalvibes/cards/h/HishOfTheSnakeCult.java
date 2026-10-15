package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "356")
@CardRegistration(set = "MB2", collectorNumber = "595")
public class HishOfTheSnakeCult extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("HishOfTheSnakeCult", new OracleData(
                "Hish of the Snake Cult",
                CardType.CREATURE,
                Set.of(),
                "{2}{B}{G}{U}",
                CardColor.BLACK,
                List.of(CardColor.BLACK, CardColor.GREEN, CardColor.BLUE),
                List.of(CardColor.BLACK, CardColor.GREEN, CardColor.BLUE),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.SNAKE),
                "Nagas and Serpents you control are Snakes. (We'll errata this to be true.)\n"
                        + "Snakes you control have daunt, deathtouch, and poisonous 2. (A creature with daunt "
                        + "can't be blocked by creatures with power 2 or less. Whenever a creature with poisonous "
                        + "2 deals combat damage to a player, that player gets two poison counters.)",
                2,
                5,
                Set.of(),
                null,
                null,
                null));
    }

    public HishOfTheSnakeCult() {
        var nagasOrSerpents = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.NAGA, CardSubtype.SERPENT));
        var snakes = new PermanentHasSubtypePredicate(CardSubtype.SNAKE);

        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.SNAKE, GrantScope.ALL_OWN_CREATURES, false, nagasOrSerpents));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.DEATHTOUCH, Keyword.POISONOUS), GrantScope.ALL_OWN_CREATURES, snakes));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentPowerAtMostPredicate(2)),
                GrantScope.ALL_OWN_CREATURES,
                snakes));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER),
                GrantScope.ALL_OWN_CREATURES,
                snakes));
    }
}
