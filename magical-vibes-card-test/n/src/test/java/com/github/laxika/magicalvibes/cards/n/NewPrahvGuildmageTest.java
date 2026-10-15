package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.a.AzoriusKeyrune;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NewPrahvGuildmage.class, DrudgeBeetle.class, AxebaneGuardian.class,
        AzoriusKeyrune.class, Forest.class})
class NewPrahvGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{W}{U}: target creature gains flying until end of turn")
    void grantsFlying() {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        addFlyingMana();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        addFlyingMana();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        Permanent bears = detain(new DrudgeBeetle());

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't block")
    void detainedCreatureCannotBlock() {
        detain(new DrudgeBeetle());

        Permanent attacker = addCreatureReady(player1, new DrudgeBeetle());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        Permanent elves = detain(new AxebaneGuardian());
        elves.setSummoningSick(false);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Can detain a noncreature nonland permanent")
    void canDetainArtifact() {
        Permanent fountain = detain(new AzoriusKeyrune());

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2,
                        gd.playerBattlefields.get(player2.getId()).indexOf(fountain), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the Guildmage controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bears = detain(new DrudgeBeetle());

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot detain a permanent you control")
    void cannotDetainOwnPermanent() {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent ownBears = addCreatureReady(player1, new DrudgeBeetle());
        addDetainMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying can target an opponent's creature")
    void grantsFlyingToOpponentsCreature() {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        addFlyingMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying cannot target a noncreature artifact")
    void cannotGrantFlyingToNoncreature() {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune());
        addFlyingMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detain cannot target an opponent's land")
    void cannotDetainLand() {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addDetainMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detain persists at the opponent's next turn")
    void detainDoesNotExpireAtOpponentsTurn() {
        Permanent creature = detain(new DrudgeBeetle());

        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detain persists after the Guildmage leaves the battlefield")
    void detainPersistsWithoutSource() {
        Permanent creature = detain(new DrudgeBeetle());
        Permanent guildmage = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guildmage));

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detain does not prevent untapping")
    void detainedCreatureCanUntap() {
        Permanent creature = detain(new DrudgeBeetle());
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent detain(com.github.laxika.magicalvibes.model.Card targetCard) {
        addCreatureReady(player1, new NewPrahvGuildmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        addDetainMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        return target;
    }

    private void addFlyingMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void addDetainMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        declareAttackers(player2, List.of(index));
    }
}
