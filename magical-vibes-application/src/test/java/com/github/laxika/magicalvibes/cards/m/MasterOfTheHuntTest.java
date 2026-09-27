package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterOfTheHunt.class})
class MasterOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("The ability creates a 1/1 green Wolf named Wolves of the Hunt")
    void createsWolvesOfTheHunt() {
        addMasterReady(player1);
        addMasterMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = findWolves(player1).getFirst();
        assertThat(wolf.getCard().getName()).isEqualTo("Wolves of the Hunt");
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(wolf.getEffectivePower()).isEqualTo(1);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wolves of the Hunt can form a band with any number of other Wolves of the Hunt")
    void wolvesCanBandWithOtherWolves() {
        addMasterReady(player1);
        addMasterMana(player1);
        addMasterMana(player1);

        createWolf();
        createWolf();
        List<Permanent> wolves = findWolves(player1);
        wolves.forEach(wolf -> wolf.setSummoningSick(false));

        declareBand(List.of(1, 2));

        assertThat(wolves.get(0).getBandId()).isNotNull();
        assertThat(wolves.get(0).getBandId()).isEqualTo(wolves.get(1).getBandId());
    }

    @Test
    @DisplayName("A named band lets its controller divide a blocker's combat damage")
    void namedBandAttackerLetsControllerAssignBlockerDamage() {
        addMasterReady(player1);
        addMasterMana(player1);
        addMasterMana(player1);
        createWolf();
        createWolf();
        List<Permanent> wolves = findWolves(player1);
        wolves.forEach(wolf -> wolf.setSummoningSick(false));

        Permanent blocker = addMasterReady(player2);
        declareBand(List.of(1, 2));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(wolves.get(0).getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wolves.get(0));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolves.get(1));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A named band of blockers lets the defending player divide an attacker's combat damage")
    void namedBandBlockersLetDefenderAssignAttackerDamage() {
        Permanent attacker = addMasterReady(player1);
        addMasterReady(player2);
        addMasterMana(player2);
        addMasterMana(player2);
        createWolf(player2);
        createWolf(player2);
        List<Permanent> wolves = findWolves(player2);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(wolves.get(0).getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wolves.get(0));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wolves.get(1));
    }

    @Test
    @DisplayName("Wolves of the Hunt cannot use their named band with another creature")
    void namedBandRequiresMatchingNames() {
        addMasterReady(player1);
        addMasterMana(player1);
        createWolf();
        findWolves(player1).getFirst().setSummoningSick(false);
        Permanent otherCreature = addMasterReady(player1);

        beginAttackDeclaration();
        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd,
                player1,
                List.of(1, 2),
                null,
                List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    private void createWolf() {
        createWolf(player1);
    }

    private void createWolf(Player player) {
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addMasterReady(Player player) {
        return addCreatureReady(player, new MasterOfTheHunt());
    }

    private void addMasterMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 2);
    }

    private List<Permanent> findWolves(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .filter(permanent -> permanent.getCard().getName().equals("Wolves of the Hunt"))
                .toList();
    }

    private void beginAttackDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(List<Integer> attackerIndices) {
        beginAttackDeclaration();
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd,
                player1,
                attackerIndices,
                null,
                List.of(attackerIndices)));
    }
}
