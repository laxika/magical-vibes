package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ControlMagic.class, ColossalWhale.class, CoralMerfolk.class, Shock.class, Island.class})
class ColossalWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger exiles the chosen defending creature when accepted")
    void attackExilesDefendingCreature() {
        addReadyWhale(player1);
        harness.addToBattlefield(player2, new CoralMerfolk());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(List.of(0));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Coral Merfolk"));
    }

    @Test
    @DisplayName("Declining the may leaves the creature on the battlefield")
    void decliningLeavesCreature() {
        addReadyWhale(player1);
        harness.addToBattlefield(player2, new CoralMerfolk());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns under its owner's control when the whale leaves")
    void exiledCreatureReturnsWhenWhaleDies() {
        addReadyWhale(player1);
        harness.addToBattlefield(player2, new CoralMerfolk());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        killWhale();

        harness.assertNotOnBattlefield(player1, "Colossal Whale");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Own creatures are not offered as attack-trigger targets")
    void ownCreatureIsNotATarget() {
        addReadyWhale(player1);
        harness.addToBattlefield(player1, new CoralMerfolk());

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        harness.assertOnBattlefield(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("Whale leaving before its attack trigger resolves prevents exile")
    void sourceLeavingBeforeResolutionPreventsExile() {
        addReadyWhale(player1);
        Permanent whale = gd.playerBattlefields.get(player1.getId()).getFirst();
        whale.setMarkedDamage(4);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, whale.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Colossal Whale");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled stolen creature returns to its owner rather than its former controller")
    void stolenCreatureReturnsToOwner() {
        addReadyWhale(player1);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        gd.playerBattlefields.get(player1.getId()).remove(merfolk);
        gd.playerBattlefields.get(player2.getId()).add(merfolk);
        gd.stolenCreatures.put(merfolk.getId(), player1.getId());
        harness.addToBattlefieldAndReturn(player2, new ControlMagic()).setAttachedTo(merfolk.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        killWhale();

        harness.assertOnBattlefield(player1, "Coral Merfolk");
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing the target in response leaves nothing to exile")
    void targetLeavingBeforeResolutionPreventsExile() {
        addReadyWhale(player1);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, merfolk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Coral Merfolk");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Islandwalk prevents blocking when the defending player controls an Island")
    void islandwalkPreventsBlocking() {
        addReadyWhale(player1);
        gd.playerBattlefields.get(player1.getId()).getFirst().setAttacking(true);
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.addToBattlefield(player2, new Island());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("islandwalk");
    }

    @Test
    @DisplayName("Whale can be blocked when the defending player controls no Island")
    void whaleCanBeBlockedWithoutIsland() {
        addReadyWhale(player1);
        gd.playerBattlefields.get(player1.getId()).getFirst().setAttacking(true);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(merfolk.isBlocking()).isTrue();
    }

    private void addReadyWhale(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ColossalWhale());
        perm.setSummoningSick(false);
    }

    private void killWhale() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        UUID whaleId = harness.getPermanentId(player1, "Colossal Whale");
        for (int i = 0; i < 3; i++) {
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, whaleId);
        }
    }
}
