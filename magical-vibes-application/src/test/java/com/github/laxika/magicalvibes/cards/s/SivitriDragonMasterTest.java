package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfDominaria;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SivitriDragonMaster.class, ShivanDragon.class, GrizzlyBears.class})
class SivitriDragonMasterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes attacks against you and your planeswalkers cost 2 life per creature")
    void plusOneTaxesAttacksAgainstPlayerAndPlaneswalker() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Permanent firstAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 0, null, null);
        harness.passBothPriorities();
        gd.playerLifeTotals.put(player2.getId(), 10);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player2,
                    List.of(battlefieldIndex(player2, firstAttacker), battlefieldIndex(player2, secondAttacker)),
                    Map.of(battlefieldIndex(player2, firstAttacker), player1.getId(),
                            battlefieldIndex(player2, secondAttacker), sivitri.getId()));
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 prevents an attack when its life cost cannot be paid")
    void plusOneRejectsUnaffordableAttack() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 0, null, null);
        harness.passBothPriorities();
        gd.playerLifeTotals.put(player2.getId(), 1);

        assertThatThrownBy(() -> declareAttackersAtPlayer(player2, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life to pay attack life cost");
    }

    @Test
    @DisplayName("-3 searches for a Dragon card and puts it into hand")
    void minusThreeSearchesForDragon() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Card dragon = new ShivanDragon();
        Card nonDragon = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonDragon, dragon));

        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("-7 destroys every non-Dragon creature")
    void minusSevenDestroysNonDragons() {
        Permanent sivitri = addReadySivitri(player1, 7);
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        Permanent nonDragon = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragon).doesNotContain(nonDragon);
    }

    @Test
    @DisplayName("+1 still taxes attacks after Sivitri leaves the battlefield")
    void plusOnePersistsAfterSivitriLeaves() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 0, null, null);
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sivitri);
        harness.setLife(player2, 10);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackersAtPlayer(player2, attacker));

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("-3 allows failing to find even when a Dragon is in the library")
    void minusThreeMayFailToFind() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Card dragon = new ShivanDragon();
        harness.setLibrary(player1, List.of(dragon));
        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 1, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-3 resolves without a choice when no Dragon exists")
    void minusThreeWithoutDragons() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-7 destroys your non-Dragons too and preserves your Dragons and Sivitri")
    void minusSevenAffectsBothPlayers() {
        Permanent sivitri = addReadySivitri(player1, 8);
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sivitri, dragon).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownBear.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingBear.getCard());
    }

    @Test
    @CardUsed({InvasionOfDominaria.class, SerraFaithkeeper.class})
    @DisplayName("+1 does not tax attacks against a battle you protect")
    void plusOneDoesNotTaxBattleAttacks() {
        Permanent sivitri = addReadySivitri(player1, 4);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfDominaria());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.activateAbility(player1, battlefieldIndex(player1, sivitri), 0, null, null);
        harness.passBothPriorities();
        harness.setLife(player2, 10);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            int attackerIndex = battlefieldIndex(player2, attacker);
            gs.declareAttackers(gd, player2, List.of(attackerIndex),
                    Map.of(attackerIndex, battle.getId()));
        });

        harness.assertLife(player2, 10);
    }

    private Permanent addReadySivitri(Player player, int loyalty) {
        Permanent sivitri = addCreatureReady(player, new SivitriDragonMaster());
        sivitri.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return sivitri;
    }

    private void declareAttackersAtPlayer(Player player, Permanent attacker) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, List.of(battlefieldIndex(player, attacker)));
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
