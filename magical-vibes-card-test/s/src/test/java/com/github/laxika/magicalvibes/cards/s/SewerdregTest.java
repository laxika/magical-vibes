package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sewerdreg.class, SellSwordBrute.class, Swamp.class})
class SewerdregTest extends BaseCardTest {

    @Test
    @DisplayName("Swampwalk prevents blocking while the defending player controls a Swamp")
    void swampwalkPreventsBlockingWithSwamp() {
        Permanent attacker = addCreatureReady(player1, new Sewerdreg());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SellSwordBrute());
        harness.addToBattlefield(player2, new Swamp());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Swampwalk allows blocking when the defending player controls no Swamp")
    void swampwalkAllowsBlockingWithoutSwamp() {
        Permanent attacker = addCreatureReady(player1, new Sewerdreg());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SellSwordBrute());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself and exiles a target card from an opponent's graveyard")
    void sacrificesItselfAndExilesCardFromOpponentsGraveyard() {
        Card target = new SellSwordBrute();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new Sewerdreg());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sewerdreg");
        harness.assertInGraveyard(player1, "Sewerdreg");
        harness.assertNotInGraveyard(player2, "Sell-Sword Brute");
        assertThat(exiledCards(player2)).contains(target);
    }

    @Test
    @DisplayName("Can exile a target card from its controller's graveyard")
    void exilesCardFromOwnGraveyard() {
        Card target = new SellSwordBrute();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new Sewerdreg());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Sell-Sword Brute");
        assertThat(exiledCards(player1)).contains(target);
    }

    @Test
    @DisplayName("Still sacrifices itself when the target leaves the graveyard before resolution")
    void sacrificesItselfWhenTargetLeavesBeforeResolution() {
        Card target = new SellSwordBrute();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new Sewerdreg());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.addToBattlefield(player2, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sewerdreg");
        harness.assertOnBattlefield(player2, "Sell-Sword Brute");
        assertThat(exiledCards(player2)).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a card that is not in a graveyard")
    void cannotTargetBattlefieldCard() {
        Card target = new SellSwordBrute();
        harness.addToBattlefield(player2, target);
        harness.addToBattlefield(player1, new Sewerdreg());

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Exiles a land card and sacrifices before resolution even while tapped and summoning sick")
    void exilesLandWhileTappedAndSummoningSick() {
        Card target = new Swamp();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new Sewerdreg());
        Permanent source = findPermanent(player1, "Sewerdreg");
        source.tap();
        source.setSummoningSick(true);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Sewerdreg");
        harness.assertInGraveyard(player1, "Sewerdreg");
        harness.assertInGraveyard(player2, "Swamp");
        assertThat(exiledCards(player2)).doesNotContain(target);

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Swamp");
        assertThat(exiledCards(player2)).contains(target);
    }

    @Test
    @DisplayName("Cannot sacrifice itself without announcing a graveyard target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new Sewerdreg());

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sewerdreg");
        harness.assertNotInGraveyard(player1, "Sewerdreg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself before paying its sacrifice cost")
    void cannotTargetItselfBeforeSacrifice() {
        Card source = new Sewerdreg();
        harness.addToBattlefield(player1, source);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sewerdreg");
        harness.assertNotInGraveyard(player1, "Sewerdreg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot announce more than one graveyard target")
    void cannotTargetTwoCards() {
        Card first = new Swamp();
        Card second = new Swamp();
        harness.setGraveyard(player2, List.of(first, second));
        harness.addToBattlefield(player1, new Sewerdreg());

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sewerdreg");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void attackersSwampDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new Sewerdreg());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SellSwordBrute());
        harness.addToBattlefield(player1, new Swamp());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private List<Card> exiledCards(Player player) {
        GameData gameData = harness.getGameData();
        return gameData.getPlayerExiledCards(player.getId());
    }
}
