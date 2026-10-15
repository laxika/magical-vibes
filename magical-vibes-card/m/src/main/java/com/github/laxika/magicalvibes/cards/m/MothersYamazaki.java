package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenControllerControlsExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "359")
@CardRegistration(set = "MB2", collectorNumber = "598")
public class MothersYamazaki extends Card {

    private static final String CARD_NAME = "Mothers Yamazaki";

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("MothersYamazaki", new OracleData(
                CARD_NAME,
                CardType.CREATURE,
                Set.of(),
                "{2}{R}{W}",
                CardColor.WHITE,
                List.of(CardColor.WHITE, CardColor.RED),
                List.of(CardColor.WHITE, CardColor.RED),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.HUMAN, CardSubtype.SAMURAI),
                "Partner with itself (When this enters, target player may put Mothers Yamazaki into their hand "
                        + "from their library, then shuffle. A Commander deck can include two of this card, and "
                        + "they can be your commanders.)\n"
                        + "As long as you control exactly two permanents named Mothers Yamazaki, the \"legend rule\" "
                        + "doesn't apply to them, and Samurai you control get +2/+2 and have vigilance and haste.",
                2,
                2,
                Set.of(Keyword.PARTNER),
                null,
                null,
                null));
    }

    public MothersYamazaki() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(CARD_NAME),
                "Have target player put Mothers Yamazaki into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleWhenControllerControlsExactlyTwoSameNameEffect());

        var exactlyTwo = new AllConditions(List.of(
                new ControlsPermanentCount(2, new PermanentNamedPredicate(CARD_NAME)),
                new ControlsPermanentCountAtMost(2, new PermanentNamedPredicate(CARD_NAME))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(exactlyTwo,
                new StaticBoostEffect(2, 2, Set.of(Keyword.VIGILANCE, Keyword.HASTE),
                        GrantScope.ALL_OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.SAMURAI))));
    }
}
