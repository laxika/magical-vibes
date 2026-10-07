package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrueheartDuelist.class, Colossapede.class})
class TrueheartDuelistTest extends BaseCardTest {

    private int addDuelist() {
        Permanent perm = harness.addToBattlefieldAndReturn(player2, new TrueheartDuelist());
        perm.setSummoningSick(false);
        return gd.playerBattlefields.get(player2.getId()).indexOf(perm);
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new Colossapede());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }
    }

    @Test
    @DisplayName("Trueheart Duelist can block two attackers")
    void canBlockTwoAttackers() {
        int duelistIdx = addDuelist();
        addAttackers(2);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(duelistIdx, 0),
                new BlockerAssignment(duelistIdx, 1)
        ));

        Permanent duelistPerm = gd.playerBattlefields.get(player2.getId()).get(duelistIdx);
        assertThat(duelistPerm.isBlocking()).isTrue();
        assertThat(duelistPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Trueheart Duelist cannot block three attackers")
    void cannotBlockThreeAttackers() {
        int duelistIdx = addDuelist();
        addAttackers(3);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(duelistIdx, 0),
                new BlockerAssignment(duelistIdx, 1),
                new BlockerAssignment(duelistIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TrueheartDuelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Trueheart Duelist");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Trueheart Duelist"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie Human Warrior token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trueheart Duelist") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalm can only be activated at sorcery speed")
    void embalmOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new TrueheartDuelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Trueheart Duelist");
    }

    @Test
    void embalmTokenCanBlockTwoAttackers() {
        setUpEmbalm();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();

        for (int i = 0; i < 2; i++) {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new Colossapede());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(token.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    void additionalBlockDoesNotApplyToOtherCreatures() {
        addDuelist();
        harness.addToBattlefield(player2, new Colossapede());
        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    void embalmCannotBeActivatedDuringOwnCombat() {
        setUpEmbalm();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Trueheart Duelist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void embalmCannotBeActivatedWithAnotherAbilityOnStack() {
        setUpEmbalm();
        harness.setGraveyard(player1, List.of(new TrueheartDuelist(), new TrueheartDuelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Trueheart Duelist");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void embalmRequiresWhiteMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TrueheartDuelist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Trueheart Duelist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void embalmRequiresThreeManaInTotal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TrueheartDuelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Trueheart Duelist");
        assertThat(gd.stack).isEmpty();
    }
}
