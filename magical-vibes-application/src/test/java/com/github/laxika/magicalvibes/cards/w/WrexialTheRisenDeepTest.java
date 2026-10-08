package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrexialTheRisenDeep.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Shock.class, Island.class, Swamp.class, Harrow.class})
class WrexialTheRisenDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers only instant and sorcery cards from the damaged player's graveyard")
    void combatDamageOffersOnlyDamagedPlayersInstantsAndSorceries() {
        Card ownInstant = new Shock();
        Card opponentInstant = new Shock();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opponentInstant, opponentCreature));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentInstant.getId());
    }

    @Test
    @DisplayName("Combat damage with no instant or sorcery in the damaged player's graveyard does not prompt")
    void noValidTargetDoesNotPrompt() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        attackDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("May cast resolves a targeted instant for free and exiles it")
    void castsTargetedInstantForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Declining the may-cast leaves the targeted card in the damaged player's graveyard")
    void decliningMayCastLeavesCardInGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(counsel.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("A sorcery can be cast during combat for free and draws cards for Wrexial's controller")
    void castsSorceryDuringCombat() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Shock();
        harness.setGraveyard(player2, List.of(counsel));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        harness.assertNotInGraveyard(player2, "Counsel of the Soratami");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(counsel);
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution cannot be cast")
    void removedTargetCannotBeCast() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Casting Harrow still requires sacrificing a land as an additional cost")
    void castingRequiresMandatoryAdditionalCost() {
        Harrow harrow = new Harrow();
        harness.setGraveyard(player2, List.of(harrow));
        harness.addToBattlefield(player1, new Island());
        UUID landId = harness.getPermanentId(player1, "Island");

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(harrow.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(landId);
        harness.handlePermanentChosen(player1, landId);
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Islandwalk prevents blocking when the defender controls an Island")
    void islandwalkPreventsBlocking() {
        assertBlockingWithDefendingLand(new Island(), false);
    }

    @Test
    @DisplayName("Swampwalk prevents blocking when the defender controls a Swamp")
    void swampwalkPreventsBlocking() {
        assertBlockingWithDefendingLand(new Swamp(), false);
    }

    @Test
    @DisplayName("Wrexial can be blocked when the defender controls neither an Island nor a Swamp")
    void canBeBlockedWithoutMatchingLand() {
        assertBlockingWithDefendingLand(null, true);
    }

    private void assertBlockingWithDefendingLand(Card land, boolean expected) {
        if (land != null) {
            harness.addToBattlefield(player2, land);
        }
        Permanent attacker = addCreatureReady(player1, new WrexialTheRisenDeep());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, attacker, gd.playerBattlefields.get(player2.getId()))).isEqualTo(expected);
    }

    private void attackDealingDamage() {
        Permanent wrexial = addCreatureReady(player1, new WrexialTheRisenDeep());
        wrexial.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
