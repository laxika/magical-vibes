package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JennyFlint;
import com.github.laxika.magicalvibes.cards.k.KateStewart;
import com.github.laxika.magicalvibes.cards.t.TheSecondDoctor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadameVastra.class, JennyFlint.class, GrizzlyBears.class,
        KateStewart.class, TheSecondDoctor.class, DressDown.class})
class MadameVastraTest extends BaseCardTest {

    @Test
    void partnerWithLetsTheTargetPlayerSearchForJennyFlint() {
        Card jenny = new JennyFlint();
        harness.setLibrary(player2, List.of(jenny));

        resolvePartnerTriggerTargeting(player2);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(jenny);
    }

    private void resolvePartnerTriggerTargeting(Player targetPlayer) {
        harness.enterBattlefieldAndReturn(player1, new MadameVastra());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).contains(targetPlayer.getId());
        harness.handlePermanentChosen(player1, targetPlayer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(targetPlayer.getId());
    }

    @Test
    void mustBeBlockedIfAble() {
        addCreatureReady(player1, new MadameVastra());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void createsAClueAndFoodWhenACreatureItDamagedDies() {
        addCreatureReady(player1, new MadameVastra());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void partnerCanTargetItsController() {
        Card jenny = new JennyFlint();
        harness.setLibrary(player1, List.of(jenny));

        resolvePartnerTriggerTargeting(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(jenny);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(jenny);
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card jenny = new JennyFlint();
        harness.setLibrary(player2, List.of(jenny));

        resolvePartnerTriggerTargeting(player2);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(jenny);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(jenny);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void partnerSearchCanFinishWithoutFindingJenny() {
        Card otherCard = new TheSecondDoctor();
        harness.setLibrary(player2, List.of(otherCard));

        resolvePartnerTriggerTargeting(player2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    void canRemainUnblockedWhenTheOnlyCreatureIsTapped() {
        addCreatureReady(player1, new MadameVastra());
        addCreatureReady(player2, new TheSecondDoctor()).tap();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void oneBlockerSatisfiesRequirementEvenWhenAnotherCanBlock() {
        addCreatureReady(player1, new MadameVastra());
        Permanent first = addCreatureReady(player2, new TheSecondDoctor());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isFalse();
    }

    @Test
    void createsTokensWhenVastraAndDamagedCreatureDieSimultaneously() {
        addCreatureReady(player1, new MadameVastra());
        addCreatureReady(player2, new KateStewart());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Madame Vastra");
        harness.assertInGraveyard(player2, "Kate Stewart");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void createsTokensWhenDamagedCreatureIsSacrificedLaterThisTurn() {
        addCreatureReady(player1, new MadameVastra());
        Permanent blocker = addCreatureReady(player2, new TheSecondDoctor());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "The Second Doctor");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, blocker));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "The Second Doctor");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void noTokensWhenAnUndamagedCreatureDies() {
        addCreatureReady(player1, new MadameVastra());
        Permanent other = addCreatureReady(player2, new TheSecondDoctor());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, other));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "The Second Doctor");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void noTokensWhenVastraHasLostHerAbilitiesBeforeDamagedCreatureDies() {
        addCreatureReady(player1, new MadameVastra());
        Permanent blocker = addCreatureReady(player2, new TheSecondDoctor());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Madame Vastra");
        harness.assertOnBattlefield(player2, "The Second Doctor");
        harness.addToBattlefield(player2, new DressDown());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, blocker));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "The Second Doctor");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }
}
