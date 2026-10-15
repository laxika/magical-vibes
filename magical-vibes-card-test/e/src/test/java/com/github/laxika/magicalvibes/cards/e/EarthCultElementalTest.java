package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthCultElemental.class, HillGiantHerdgorger.class, Mountain.class})
class EarthCultElementalTest extends BaseCardTest {

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
    @DisplayName("A result from 1 through 9 makes each player sacrifice a permanent")
    void lowRollMakesEachPlayerSacrifice() {
        setRoll(9);
        harness.addToBattlefield(player2, new Mountain());

        castEarthCultElemental();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Mountain);
    }

    @Test
    @DisplayName("A result from 10 through 19 makes each opponent sacrifice a permanent")
    void middleRollMakesOpponentsSacrifice() {
        setRoll(10);
        harness.addToBattlefield(player1, new HillGiantHerdgorger());
        harness.addToBattlefield(player2, new Mountain());

        castEarthCultElemental();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HillGiantHerdgorger);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Mountain);
    }

    @Test
    @DisplayName("A result of 20 makes each opponent sacrifice two permanents")
    void criticalRollMakesOpponentsSacrificeTwo() {
        setRoll(20);
        harness.addToBattlefield(player1, new HillGiantHerdgorger());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castEarthCultElemental();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HillGiantHerdgorger);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("A player chooses among multiple permanents for the sacrifice")
    void multiplePermanentsPromptForChoice() {
        setRoll(10);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castEarthCultElemental();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("A result of 1 also makes the controller sacrifice the Elemental")
    void minimumRollMakesEachPlayerSacrifice() {
        setRoll(1);
        harness.addToBattlefield(player2, new Mountain());

        castEarthCultElemental();

        harness.assertInGraveyard(player1, "Earth-Cult Elemental");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A result of 19 sacrifices only one opposing permanent")
    void highestMiddleRollSacrificesOnlyOnePermanent() {
        setRoll(19);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castEarthCultElemental();

        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
        harness.assertOnBattlefield(player1, "Earth-Cult Elemental");
    }

    @Test
    @DisplayName("On a low roll players choose before any permanent is sacrificed")
    void lowRollCollectsBothPlayersChoicesBeforeSacrificing() {
        setRoll(9);
        Permanent ownMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castEarthCultElemental();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownMountain.getId()));

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingMountain.getId()));

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Earth-Cult Elemental");
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("On a result of 20 the opponent chooses two out of three permanents")
    void criticalRollLetsOpponentChooseTwoPermanents() {
        setRoll(20);
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castEarthCultElemental();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(firstMountain.getId(), secondMountain.getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Earth-Cult Elemental");
    }

    @Test
    @DisplayName("On a result of 20 an opponent with one permanent sacrifices it")
    void criticalRollSacrificesAsManyAsPossible() {
        setRoll(20);
        harness.addToBattlefield(player2, new Mountain());

        castEarthCultElemental();

        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Earth-Cult Elemental");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent with no permanents does not prevent the ability from resolving")
    void criticalRollWithEmptyOpposingBattlefieldResolves() {
        setRoll(20);

        castEarthCultElemental();

        harness.assertOnBattlefield(player1, "Earth-Cult Elemental");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private void castEarthCultElemental() {
        harness.setHand(player1, List.of(new EarthCultElemental()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
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
