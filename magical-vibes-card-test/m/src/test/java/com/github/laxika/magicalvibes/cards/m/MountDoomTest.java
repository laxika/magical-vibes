package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BilbosRing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MithrilCoat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MountDoom.class, GrizzlyBears.class, LlanowarElves.class, BilbosRing.class, LiquimetalCoating.class, MithrilCoat.class})
class MountDoomTest extends BaseCardTest {

    @Test
    void addsBlackOrRedManaAndCostsOneLife() {
        Permanent mountDoom = addReadyMountDoom(player1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(mountDoom.isTapped()).isTrue();
    }

    @Test
    void dealsOneDamageToEachOpponent() {
        addReadyMountDoom(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void sacrificesSourceAndLegendaryArtifactThenKeepsUpToTwoCreatures() {
        Permanent mountDoom = addReadyMountDoom(player1);
        Card legendaryArtifact = legendaryArtifact();
        harness.addToBattlefield(player1, legendaryArtifact);
        Permanent firstKept = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondKept = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DestroyRestChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstKept.getId(), secondKept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstKept);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondKept);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mountDoom);
        harness.assertInGraveyard(player1, "Mount Doom");
        harness.assertInGraveyard(player1, "Bilbo's Ring");
    }

    @Test
    void cannotActivateDestructionAbilityWithoutLegendaryArtifact() {
        addReadyMountDoom(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseRedManaWithoutUsingTheStack() {
        Permanent mountDoom = addReadyMountDoom(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(mountDoom.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateManaAbilityWhenTapped() {
        Permanent mountDoom = addReadyMountDoom(player1);
        mountDoom.tap();
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mountDoom.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void canChooseNoCreaturesAndDestroyAllOfThem() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        addDestructionMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertNotOnBattlefield(player1, "Mount Doom");
        harness.assertNotOnBattlefield(player1, "Bilbo's Ring");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void canKeepExactlyOneOpponentsCreature() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        addDestructionMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(kept.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(kept);
    }

    @Test
    void destructionResolvesWithNoCreatures() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LiquimetalCoating());
        addDestructionMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mount Doom");
        harness.assertInGraveyard(player1, "Bilbo's Ring");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsLegendaryArtifact() {
        Permanent mountDoom = addReadyMountDoom(player1);
        harness.addToBattlefield(player2, new BilbosRing());
        addDestructionMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountDoom);
        harness.assertOnBattlefield(player2, "Bilbo's Ring");
    }

    @Test
    void cannotActivateDestructionDuringUpkeep() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        addDestructionMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mount Doom");
        harness.assertOnBattlefield(player1, "Bilbo's Ring");
    }

    @Test
    void artifactMountDoomCannotPayBothSacrificeRequirements() {
        Permanent mountDoom = addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new LiquimetalCoating());

        harness.activateAbility(player1, 1, 0, null, mountDoom.getId());
        harness.passBothPriorities();
        addDestructionMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountDoom);
        assertThat(mountDoom.isTapped()).isFalse();
    }

    @Test
    void unchosenIndestructibleCreatureAndNoncreatureArtifactSurvive() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent coat = harness.addToBattlefieldAndReturn(player2, new MithrilCoat());
        coat.setAttachedTo(survivor.getId());
        addDestructionMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor, coat);
    }

    @Test
    void cannotActivateDestructionDuringOpponentsMainPhase() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        addDestructionMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mount Doom");
        harness.assertOnBattlefield(player1, "Bilbo's Ring");
    }

    @Test
    void cannotActivateDestructionWhileAnotherAbilityIsOnTheStack() {
        addReadyMountDoom(player1);
        harness.addToBattlefield(player1, new BilbosRing());
        addReadyMountDoom(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, 1, null, null);
        addDestructionMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mount Doom");
        harness.assertOnBattlefield(player1, "Bilbo's Ring");
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private void addDestructionMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addReadyMountDoom(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MountDoom());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private static Card legendaryArtifact() {
        return new BilbosRing();
    }
}
