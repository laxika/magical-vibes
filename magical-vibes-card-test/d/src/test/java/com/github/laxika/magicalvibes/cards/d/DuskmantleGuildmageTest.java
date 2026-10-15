package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeckonApparition;
import com.github.laxika.magicalvibes.cards.c.CorpseBlockade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskmantleGuildmage.class, GrizzlyBears.class, Shock.class,
        BeckonApparition.class, CorpseBlockade.class, LeylineOfSanctity.class})
class DuskmantleGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Second ability mills two cards from target player's library")
    void millsTwoCards() {
        addGuildmage(player1);
        addManaFor(player1, 4);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("First ability drains 1 life for each card put into an opponent's graveyard")
    void drainsForEachCardInOpponentGraveyard() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 4);

        activateDrain();

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("First ability ignores cards put into the controller's own graveyard")
    void ignoresOwnGraveyard() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 4);

        activateDrain();

        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Two activations of the first ability drain twice per card")
    void activationsStack() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 3);
        addManaFor(player1, 4);

        activateDrain();
        activateDrain();

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("First ability fires for cards entering the graveyard from the battlefield")
    void firesForDyingCreature() {
        addGuildmage(player1);
        addManaFor(player1, 3);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        activateDrain();

        int lifeBefore = gd.getLife(player2.getId());

        killWithShock(bears);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("First ability stops draining after the turn ends")
    void wearsOffAtEndOfTurn() {
        addGuildmage(player1);
        addManaFor(player1, 3);

        activateDrain();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player2.getId());

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        killWithShock(bears);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Dying tokens do not trigger life loss because tokens are not cards")
    void ignoresDyingTokens() {
        DuskmantleGuildmage exiledCard = new DuskmantleGuildmage();
        harness.setGraveyard(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new BeckonApparition()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, exiledCard.getId());
        Permanent spirit = findPermanent(player2, "Spirit");

        addGuildmage(player1);
        addManaFor(player1, 3);
        harness.ensurePriority(player1);
        activateDrain();
        int lifeBefore = gd.getLife(player2.getId());

        killWithShock(spirit);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Spirit");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life loss does not target and affects an opponent with hexproof")
    void lifeLossIgnoresPlayerHexproof() {
        addGuildmage(player1);
        harness.addToBattlefield(player2, new CorpseBlockade());
        harness.addToBattlefield(player2, new DuskmantleGuildmage());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        addManaFor(player1, 3);
        activateDrain();
        int lifeBefore = gd.getLife(player2.getId());

        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Duskmantle Guildmage");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The delayed ability continues after the Guildmage leaves the battlefield")
    void persistsAfterSourceDies() {
        Permanent guildmage = addGuildmage(player1);
        addManaFor(player1, 3);
        activateDrain();
        killWithShock(guildmage);
        resolveAllTriggers();
        int lifeBefore = gd.getLife(player2.getId());

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        killWithShock(bears);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Duskmantle Guildmage");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Milling a one-card library loses only one life and does not cause a draw loss")
    void millsOnlyAvailableCards() {
        addGuildmage(player1);
        harness.setLibrary(player2, List.of(new DuskmantleGuildmage()));
        addManaFor(player1, 3);
        addManaFor(player1, 4);
        activateDrain();
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An opponent's resolved instant triggers life loss even after killing the Guildmage")
    void triggersForResolvedOpponentSpell() {
        Permanent guildmage = addGuildmage(player1);
        addManaFor(player1, 3);
        activateDrain();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0, guildmage.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotOnBattlefield(player1, "Duskmantle Guildmage");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Cards milled before the first ability resolves do not trigger it retroactively")
    void doesNotWatchBeforeResolution() {
        addGuildmage(player1);
        addManaFor(player1, 3);
        addManaFor(player1, 4);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    private void activateDrain() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    /** Puts the creature into its owner's graveyard through the normal death pipeline. */
    private void killWithShock(Permanent creature) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }

    private Permanent addGuildmage(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DuskmantleGuildmage());
    }

    private void addManaFor(Player player, int generic) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.WHITE, generic - 2);
    }
}
