package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({Skulduggery.class, GrizzlyBears.class, LlanowarElves.class})
class SkulduggeryTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gets +1/+1 and target opponent creature gets -1/-1")
    void boostsOwnAndDebuffsOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, List.of(ownId, oppId));

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);

        Permanent opp = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opp.getPowerModifier()).isEqualTo(-1);
        assertThat(opp.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("-1/-1 kills a 1/1 creature")
    void debuffKillsOneOneCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(ownId, oppId));

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player2, bear2);
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Boost still applies when second target removed before resolution")
    void boostAppliesWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void modifiersExpireAtCleanup() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opponent.getId()));
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(opponent.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    void cannotCastWithOnlyOneTarget() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(own.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstTargetChangingControllerDoesNotReceiveBoost() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(own.getId(), opponent.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(own);
        gd.playerBattlefields.get(player2.getId()).add(own);
        harness.passBothPriorities();

        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isEqualTo(-1);
        assertThat(opponent.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void secondTargetChangingControllerDoesNotReceiveDebuff() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(own.getId(), opponent.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        gd.playerBattlefields.get(player1.getId()).add(opponent);
        harness.passBothPriorities();

        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Debuff still applies when first target removed before resolution")
    void debuffAppliesWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        Permanent opp = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opp.getPowerModifier()).isEqualTo(-1);
        assertThat(opp.getToughnessModifier()).isEqualTo(-1);
    }
}
