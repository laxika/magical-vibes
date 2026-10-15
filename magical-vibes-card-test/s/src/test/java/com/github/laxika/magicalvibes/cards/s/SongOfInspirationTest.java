package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SongOfInspiration.class, AirElemental.class, GrizzlyBears.class, Shock.class})
class SongOfInspirationTest extends BaseCardTest {

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
    void returnsUpToTwoPermanentCardsAndExcludesNonpermanents() {
        Card creature = new GrizzlyBears();
        Card permanent = new AirElemental();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, permanent, instant));
        Card spell = new SongOfInspiration();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        setRoll(1);

        harness.castInstant(player1, 0, List.of(creature.getId(), permanent.getId()));
        assertThat(gd.stack.getLast().getTargetCardIds()).contains(creature.getId(), permanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, spell);
    }

    @Test
    void highResultGainsLifeEqualToSelectedCardsTotalManaValue() {
        Card first = new GrizzlyBears();
        Card second = new AirElemental();
        Card unselected = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, unselected));
        Card spell = new SongOfInspiration();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 10);
        setRoll(8);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        assertThat(gd.stack.getLast().getTargetCardIds()).contains(first.getId(), second.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected, spell);
    }

    @ParameterizedTest
    @CsvSource({"7, 10", "8, 17", "20, 17"})
    void usesModifiedResultAtBothBoundariesAndAboveTwenty(int roll, int expectedLife) {
        Card first = new GrizzlyBears();
        Card second = new AirElemental();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLife(player1, 10);
        setRoll(roll);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, expectedLife);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void canResolveWithNoTargets() {
        Card spell = new SongOfInspiration();
        harness.setLife(player1, 10);
        setRoll(20);

        harness.castFromHand(player1, spell, "{3}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @ParameterizedTest
    @CsvSource({"8, 10, true", "13, 12, true", "8, 10, false", "13, 12, false"})
    void ignoresManaValueOfTargetThatLeftTheGraveyardBeforeResolution(int roll, int expectedLife, boolean exiled) {
        Card remaining = new GrizzlyBears();
        Card departed = new AirElemental();
        harness.setGraveyard(player1, List.of(remaining, departed));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLife(player1, 10);
        setRoll(roll);

        harness.castInstant(player1, 0, List.of(remaining.getId(), departed.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        if (exiled) {
            harness.setExile(player1, List.of(departed));
        } else {
            harness.setHand(player1, List.of(departed));
        }
        harness.passBothPriorities();

        harness.assertLife(player1, expectedLife);
        if (exiled) {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
            assertThat(gd.findExiledCard(departed.getId())).isNotNull();
        } else {
            assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(remaining, departed);
        }
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeavesTheGraveyard() {
        Card target = new GrizzlyBears();
        Card spell = new SongOfInspiration();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLife(player1, 10);
        setRoll(20);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void returnsOneTargetAndGainsItsManaValue() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLife(player1, 10);
        setRoll(13);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
    }

    @Test
    void cannotTargetAnInstant() {
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAnOpponentsPermanentCard() {
        Card opposing = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opposing));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opposing.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
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
