package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.f.FacetReader;
import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GusthasScepter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EscapeProtocol.class, Censor.class, GrizzlyBears.class, GusthasScepter.class,
        DrannithHealer.class, FacetReader.class, AlmightyBrushwagg.class,
        GrafRats.class, MidnightScavengers.class, ChitteringHost.class})
class EscapeProtocolTest extends BaseCardTest {

    @Test
    void paysThenChoosesAnArtifactOrCreatureYouControl() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    void canFlickerAnArtifact() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(harness.getPermanentId(player1, "Gustha's Scepter")).isNotEqualTo(artifact.getId());
    }

    @Test
    void decliningPaymentDoesNotFlicker() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(creature.getId());
    }

    @Test
    void ordinaryDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        addCreatureReady(player1, new FacetReader());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Drannith Healer");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        harness.setHand(player2, List.of(new DrannithHealer()));
        harness.setLibrary(player2, List.of(new AlmightyBrushwagg()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        harness.assertInHand(player2, "Almighty Brushwagg");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsStolenCreatureToItsOwner() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(harness.getPermanentId(player2, "Almighty Brushwagg")).isNotEqualTo(creature.getId());
    }

    @Test
    void flickeringMeldedCreatureReturnsBothFrontFaces() {
        harness.addToBattlefield(player1, new GrafRats());
        harness.addToBattlefield(player1, new MidnightScavengers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        Permanent host = findPermanent(player1, "Chittering Host");
        harness.addToBattlefield(player1, new EscapeProtocol());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, host.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Chittering Host");
        harness.assertOnBattlefield(player1, "Graf Rats");
        harness.assertOnBattlefield(player1, "Midnight Scavengers");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void canPayWithoutAnEligibleTarget() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    void targetCanLeaveBeforeReflexiveAbilityResolves() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertInGraveyard(player1, "Almighty Brushwagg");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void exiledTokenDoesNotReturn() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        AlmightyBrushwagg token = new AlmightyBrushwagg();
        token.setToken(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, token);
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertInHand(player1, "Almighty Brushwagg");
    }
}
