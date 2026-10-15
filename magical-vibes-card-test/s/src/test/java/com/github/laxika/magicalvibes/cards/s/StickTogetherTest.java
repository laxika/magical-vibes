package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.f.FallenCleric;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MistformUltimus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StickTogether.class, FallenCleric.class, DeathcultRogue.class, DromokaWarrior.class,
        FugitiveWizard.class, GrizzlyBears.class, HillGiant.class, Island.class, MistformUltimus.class})
class StickTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps up to one creature of each party role and sacrifices the rest")
    void eachPlayerChoosesPartyAndSacrificesTheRest() {
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new DeathcultRogue());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent extraCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new FallenCleric());
        Permanent opponentRogue = harness.addToBattlefieldAndReturn(player2, new DeathcultRogue());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new DromokaWarrior());
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent opponentExtra = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        chooseParty(player1, cleric, rogue, warrior, wizard);
        chooseParty(player2, opponentCleric, opponentRogue, opponentWarrior, opponentWizard);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(cleric, rogue, warrior, wizard, land)
                .doesNotContain(extraCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentCleric, opponentRogue, opponentWarrior, opponentWizard)
                .doesNotContain(opponentExtra);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A player may choose no party members")
    void mayChooseNoPartyMembers() {
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        Permanent extraCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(cleric, extraCreature);
        harness.assertInGraveyard(player1, "Fallen Cleric");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only the chosen creature of a duplicated party role survives")
    void choosesOneOfTwoClerics() {
        Permanent firstCleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        Permanent secondCleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondCleric.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondCleric)
                .doesNotContain(firstCleric);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Fallen Cleric"))
                .hasSize(1);
    }

    @Test
    @DisplayName("A player may decline a role and still choose a later role")
    void mayDeclineClericAndKeepWizard() {
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(wizard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(wizard)
                .doesNotContain(cleric);
        harness.assertInGraveyard(player1, "Fallen Cleric");
    }

    @Test
    @DisplayName("Players with no eligible party members sacrifice all their creatures")
    void sacrificesCreaturesWithoutPartyRolesWithoutPrompting() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Stick Together");
    }

    @Test
    @DisplayName("A creature with every creature type may fill one chosen party role")
    void creatureWithEveryTypeFillsOnlyOneRole() {
        Permanent allTypes = harness.addToBattlefieldAndReturn(player1, new MistformUltimus());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new DeathcultRogue());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(allTypes.getId()));
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(allTypes.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(rogue.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(warrior.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(wizard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(allTypes, rogue, warrior, wizard)
                .doesNotContain(cleric);
        harness.assertInGraveyard(player1, "Fallen Cleric");
    }

    @Test
    @DisplayName("No creatures are sacrificed until both players finish choosing")
    void waitsForBothPlayersBeforeSacrificing() {
        Permanent ownCleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        Permanent ownExtra = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new FallenCleric());
        Permanent opponentExtra = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCleric.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCleric, ownExtra);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCleric, opponentExtra);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Hill Giant");

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentCleric.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownCleric);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCleric);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    private void cast() {
        harness.castFromHand(player1, new StickTogether(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private void chooseParty(com.github.laxika.magicalvibes.model.Player player,
                             Permanent cleric, Permanent rogue, Permanent warrior, Permanent wizard) {
        harness.handleMultiplePermanentsChosen(player, List.of(cleric.getId()));
        harness.handleMultiplePermanentsChosen(player, List.of(rogue.getId()));
        harness.handleMultiplePermanentsChosen(player, List.of(warrior.getId()));
        harness.handleMultiplePermanentsChosen(player, List.of(wizard.getId()));
    }
}
