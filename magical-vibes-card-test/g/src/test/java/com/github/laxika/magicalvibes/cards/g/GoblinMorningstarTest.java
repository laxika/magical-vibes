package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMorningstar.class})
class GoblinMorningstarTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void lowRollCreatesAnUnattachedGoblin() {
        Permanent morningstar = castMorningstar(9);
        Permanent goblin = findGoblin();

        assertThat(morningstar.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void highRollCreatesAndEquipsTheGoblin() {
        Permanent morningstar = castMorningstar(10);
        Permanent goblin = findGoblin();

        assertThat(morningstar.getAttachedTo()).isEqualTo(goblin.getId());
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void minimumRollCreatesExactlyOneUnattachedGoblin() {
        Permanent morningstar = castMorningstar(1);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        assertThat(morningstar.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, findGoblin())).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, findGoblin(), Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void naturalTwentyCreatesExactlyOneEquippedGoblin() {
        Permanent morningstar = castMorningstar(20);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        Permanent goblin = findGoblin();
        assertThat(morningstar.getAttachedTo()).isEqualTo(goblin.getId());
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void lowRollGoblinCanBeEquippedByPayingTheEquipCost() {
        Permanent morningstar = castMorningstar(9);
        Permanent goblin = findGoblin();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(morningstar),
                null, goblin.getId());
        resolveAllTriggers();

        assertThat(morningstar.getAttachedTo()).isEqualTo(goblin.getId());
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reequippingMovesTheBonusAndDoesNotMoveAnotherMorningstar() {
        Permanent firstMorningstar = castMorningstar(10);
        Permanent firstGoblin = findGoblin();
        Permanent secondMorningstar = castMorningstar(10);
        Permanent secondGoblin = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> !permanent.getId().equals(firstGoblin.getId()))
                .findFirst().orElseThrow();
        assertThat(firstMorningstar.getAttachedTo()).isEqualTo(firstGoblin.getId());
        assertThat(secondMorningstar.getAttachedTo()).isEqualTo(secondGoblin.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstMorningstar),
                null, secondGoblin.getId());
        resolveAllTriggers();

        assertThat(firstMorningstar.getAttachedTo()).isEqualTo(secondGoblin.getId());
        assertThat(secondMorningstar.getAttachedTo()).isEqualTo(secondGoblin.getId());
        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, firstGoblin, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondGoblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, secondGoblin, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent castMorningstar(int roll) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(roll));
        GoblinMorningstar morningstar = new GoblinMorningstar();
        harness.castFromHand(player1, morningstar, "{1}{R}");
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == morningstar)
                .findFirst()
                .orElseThrow();
    }

    private Permanent findGoblin() {
        return findPermanent(player1, "Goblin");
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
