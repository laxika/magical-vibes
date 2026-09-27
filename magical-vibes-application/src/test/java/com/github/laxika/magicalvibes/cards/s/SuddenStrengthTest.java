package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorderPatrol.class, RiftstonePortal.class, SuddenStrength.class, SuntailHawk.class})
class SuddenStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 and draws a card")
    void boostsAndDraws() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        addMana();

        UUID hawkId = harness.getPermanentId(player1, "Suntail Hawk");
        harness.castAndResolveInstant(player1, 0, hawkId);

        Permanent hawk = findPermanent(player1, "Suntail Hawk");
        assertThat(hawk.getPowerModifier()).isEqualTo(3);
        assertThat(hawk.getToughnessModifier()).isEqualTo(3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature while the caster draws")
    void targetsOpponentCreatureAndDrawsForCaster() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        addMana();

        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.castAndResolveInstant(player1, 0, hawkId);

        Permanent hawk = findPermanent(player2, "Suntail Hawk");
        assertThat(hawk.getPowerModifier()).isEqualTo(3);
        assertThat(hawk.getToughnessModifier()).isEqualTo(3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Fizzles without drawing if the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        addMana();

        harness.castInstant(player1, 0, hawk.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new SuntailHawk()));
        addMana();

        UUID hawkId = harness.getPermanentId(player1, "Suntail Hawk");
        harness.castAndResolveInstant(player1, 0, hawkId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hawk = findPermanent(player1, "Suntail Hawk");
        assertThat(hawk.getPowerModifier()).isZero();
        assertThat(hawk.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new RiftstonePortal());
        harness.setHand(player1, List.of(new SuddenStrength()));
        addMana();

        UUID targetId = harness.getPermanentId(player1, "Riftstone Portal");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorderPatrol());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));
        addMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }
}
