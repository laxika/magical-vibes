package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseIndependentModesOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "292")
@CardRegistration(set = "MB2", collectorNumber = "528")
public class CatchOfTheDay extends Card {

    private static final String VIGILANCE = "Vigilance";
    private static final String WARD = "Ward {3}";
    private static final String ISLANDWALK = "Islandwalk";
    private static final String SCRY = "Scry 2";
    private static final String GOAD = "Goad target creature an opponent controls";
    private static final String TAP = "Tap target creature an opponent controls";
    private static final String SIX_TWO = "6/2";
    private static final String FOUR_FOUR = "4/4";
    private static final String TWO_SIX = "2/6";

    public CatchOfTheDay() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseIndependentModesOnEnterEffect(List.of(
                List.of(VIGILANCE, WARD, ISLANDWALK),
                List.of(SCRY, GOAD, TAP),
                List.of(SIX_TWO, FOUR_FOUR, TWO_SIX))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(VIGILANCE),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(WARD),
                new GrantKeywordEffect(Keyword.WARD, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(ISLANDWALK),
                new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.SELF)));

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new ConditionalEffect(new SourceHasChosenMode(WARD), new CounterUnlessPaysEffect(3)));

        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new SourceHasChosenMode(SCRY),
                new ScryEffect(2)));

        var opponentCreature = TargetFilters.creatureAnOpponentControls();
        target(opponentCreature).addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new SourceHasChosenMode(GOAD),
                        new GoadTargetCreatureUntilNextTurnEffect(opponentCreature.predicate())))
                .addEffect(EffectSlot.ON_ATTACK,
                        new ConditionalEffect(new SourceHasChosenMode(TAP),
                                new TapPermanentsEffect(TapUntapScope.TARGET, opponentCreature.predicate())));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(SIX_TWO),
                new SetBasePowerToughnessEffect(6, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(FOUR_FOUR),
                new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceHasChosenMode(TWO_SIX),
                new SetBasePowerToughnessEffect(2, 6, GrantScope.SELF)));
    }
}
