package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChaosWarp;
import com.github.laxika.magicalvibes.service.effect.normalfx.D4RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD4EffectHandler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BucknardsEverfullPurse.class, ChaosWarp.class})
class BucknardsEverfullPurseTest extends BaseCardTest {

    private RollD4EffectHandler rollD4EffectHandler;
    private D4RollService originalD4RollService;

    @BeforeEach
    void captureD4RollService() {
        rollD4EffectHandler = GameTestEngineContext.get().getBean(RollD4EffectHandler.class);
        originalD4RollService = (D4RollService) ReflectionTestUtils.getField(
                rollD4EffectHandler, "d4RollService");
    }

    @AfterEach
    void restoreD4RollService() {
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", originalD4RollService);
    }

    @Test
    @DisplayName("Creates Treasures equal to the d4 result, then passes the Purse to the right")
    void createsRolledTreasuresThenPassesToTheRight() {
        setRoll(3);
        Permanent purse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(purse);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(purse);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4})
    void createsTreasuresForOtherDieResults(int result) {
        setRoll(result);
        Permanent purse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(purse.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(purse);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(result);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(purse);
        assertThat(purse.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutPayingMana() {
        Permanent purse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(purse.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent purse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());
        purse.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newControllerCreatesTreasuresAndPassesPurseBack() {
        setRoll(2);
        Permanent purse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(purse);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(purse);
        assertThat(purse.isTapped()).isTrue();
    }

    @Test
    void abilityStillCreatesTreasuresButDoesNotPassReturnedPurse() {
        setRoll(4);
        Permanent originalPurse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, originalPurse.getId());

        Permanent returnedPurse = findPermanents(player1, "Bucknard's Everfull Purse").getFirst();
        assertThat(returnedPurse.getId()).isNotEqualTo(originalPurse.getId());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returnedPurse);
        harness.assertNotOnBattlefield(player2, "Bucknard's Everfull Purse");
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", new FixedD4RollService(result));
    }

    private static final class FixedD4RollService extends D4RollService {

        private final int result;

        private FixedD4RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
