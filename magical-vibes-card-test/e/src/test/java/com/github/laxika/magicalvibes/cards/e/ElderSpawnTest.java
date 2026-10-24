package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElderSpawn.class, Island.class, GrizzlyBears.class, ShivanDragon.class, Unsummon.class})
class ElderSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Without an Island, Elder Spawn is sacrificed and deals 6 damage")
    void noIslandSacrificesElderSpawnAndDealsDamage() {
        harness.addToBattlefield(player1, new ElderSpawn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elder Spawn");
        harness.assertInGraveyard(player1, "Elder Spawn");
        harness.assertLife(player1, 14);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Without an Island, Elder Spawn's death replacement is chosen before it is sacrificed or deals damage")
    void noIslandAsksForTheDeathReplacementBeforeElderSpawnIsSacrificed() {
        Permanent elderSpawn = harness.addToBattlefieldAndReturn(player1, new ElderSpawn());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(elderSpawn.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.inDepartureBatch()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elderSpawn);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();
        harness.assertLife(player1, 20);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(elderSpawn.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elderSpawn);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).hasSize(1);
        harness.assertLife(player1, 14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Declining to sacrifice an Island asks for Elder Spawn's death replacement before it is sacrificed or deals damage")
    void decliningAsksForTheDeathReplacementBeforeElderSpawnIsSacrificed() {
        Permanent elderSpawn = harness.addToBattlefieldAndReturn(player1, new ElderSpawn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(elderSpawn.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.inDepartureBatch()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elderSpawn);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();
        harness.assertLife(player1, 20);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(elderSpawn.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elderSpawn);
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).hasSize(1);
        harness.assertLife(player1, 14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    @Test
    @DisplayName("An opponent's Island cannot pay for Elder Spawn")
    void opponentIslandCannotPay() {
        harness.addToBattlefield(player1, new ElderSpawn());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elder Spawn");
        harness.assertInGraveyard(player1, "Elder Spawn");
        harness.assertLife(player1, 14);
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Elder Spawn does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ElderSpawn());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elder Spawn");
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing an Island keeps Elder Spawn")
    void sacrificingIslandKeepsElderSpawn() {
        harness.addToBattlefield(player1, new ElderSpawn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Elder Spawn");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Declining to sacrifice an Island sacrifices Elder Spawn and deals 6 damage")
    void decliningIslandSacrificeSacrificesElderSpawn() {
        harness.addToBattlefield(player1, new ElderSpawn());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Elder Spawn");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Only the chosen Island is sacrificed when several are available")
    void choosesOneIslandToSacrifice() {
        harness.addToBattlefield(player1, new ElderSpawn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent firstIsland = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent secondIsland = gd.playerBattlefields.get(player1.getId()).get(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondIsland.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstIsland).doesNotContain(secondIsland);
        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Elder Spawn");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The upkeep ability still deals damage after Elder Spawn leaves the battlefield")
    void damageStillHappensAfterSourceReturnsToHand() {
        harness.addToBattlefield(player1, new ElderSpawn());
        var sourceId = harness.getPermanentId(player1, "Elder Spawn");
        harness.setHand(player1, List.of(new Unsummon()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, sourceId);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Elder Spawn");
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInHand(player1, "Elder Spawn");
        harness.assertNotInGraveyard(player1, "Elder Spawn");
    }

    @Test
    @DisplayName("Elder Spawn can't be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        Permanent elderSpawn = addCreatureReady(player1, new ElderSpawn());
        Permanent blocker = addCreatureReady(player2, new ShivanDragon());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(elderSpawn)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elder Spawn can be blocked by a nonred creature")
    void canBeBlockedByNonredCreature() {
        Permanent elderSpawn = addCreatureReady(player1, new ElderSpawn());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elderSpawn))));

        assertThat(blocker.isBlocking()).isTrue();
    }

}
