package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CryptSliver;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynapseSliver.class, CryptSliver.class, EnormousBaloth.class})
class SynapseSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver's combat damage lets its controller draw a card")
    void controllerMayDrawForSliverCombatDamage() {
        addAttackingCreature(player1, new SynapseSliver());
        harness.setLibrary(player1, List.of(new EnormousBaloth()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the combat-damage choice does not draw")
    void controllerMayDeclineTheDraw() {
        addAttackingCreature(player1, new SynapseSliver());
        harness.setLibrary(player1, List.of(new EnormousBaloth()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Each Sliver that deals combat damage has its own draw trigger")
    void eachSliverTriggersSeparately() {
        addAttackingCreature(player1, new SynapseSliver());
        addAttackingCreature(player1, new CryptSliver());
        harness.setLibrary(player1, List.of(new EnormousBaloth(), new EnormousBaloth()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Multiple Synapse Slivers grant multiple draw triggers")
    void multipleSynapseSliversStackTheirTriggers() {
        addCreatureReady(player1, new SynapseSliver());
        addCreatureReady(player1, new SynapseSliver());
        addAttackingCreature(player1, new CryptSliver());
        harness.setLibrary(player1, List.of(new EnormousBaloth(), new EnormousBaloth()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The ability also lets an opposing Sliver's controller draw")
    void opposingSliverControllerMayDraw() {
        addCreatureReady(player1, new SynapseSliver());
        addAttackingCreature(player2, new CryptSliver());
        harness.setLibrary(player2, List.of(new EnormousBaloth()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A non-Sliver does not gain the draw ability")
    void nonSliverDoesNotTrigger() {
        addCreatureReady(player1, new SynapseSliver());
        addAttackingCreature(player1, new EnormousBaloth());
        harness.setLibrary(player1, List.of(new EnormousBaloth()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing Sliver triggers an ability controlled by Synapse Sliver's controller")
    void opposingCombatDamageTriggerBelongsToSynapseSliver() {
        Permanent synapse = addCreatureReady(player1, new SynapseSliver());
        addAttackingCreature(player2, new CryptSliver());
        harness.setLibrary(player2, List.of(new EnormousBaloth()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getSourcePermanentId()).isEqualTo(synapse.getId());
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not trigger a draw")
    void blockedSliverDoesNotTrigger() {
        addAttackingCreature(player1, new SynapseSliver());
        Permanent blocker = addCreatureReady(player2, new EnormousBaloth());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, List.of(new EnormousBaloth()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

}
