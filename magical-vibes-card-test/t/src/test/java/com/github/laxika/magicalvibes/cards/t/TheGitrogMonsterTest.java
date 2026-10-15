package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzledAngler;
import com.github.laxika.magicalvibes.cards.h.HauntedDead;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheGitrogMonster.class, Forest.class, HauntedDead.class, GrizzledAngler.class,
        ImprisonedInTheMoon.class})
class TheGitrogMonsterTest extends BaseCardTest {

    private int handSize() {
        return gd.playerHands.get(player1.getId()).size();
    }

    @Test
    @DisplayName("Upkeep with no land sacrifices The Gitrog Monster without prompting")
    void upkeepWithoutLandSacrificesItself() {
        harness.addToBattlefield(player1, new TheGitrogMonster());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "The Gitrog Monster");
        harness.assertInGraveyard(player1, "The Gitrog Monster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a land at upkeep keeps it and draws a card for the binned land")
    void sacrificingLandKeepsItAndDraws() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new HauntedDead()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Forest").getId());

        int handBefore = handSize();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Gitrog Monster");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(handSize()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Haunted Dead");
    }

    @Test
    @DisplayName("Declining to sacrifice a land sacrifices The Gitrog Monster")
    void decliningSacrificesItself() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "The Gitrog Monster");
        harness.assertInGraveyard(player1, "The Gitrog Monster");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The sacrifice ability does not trigger during an opponent's upkeep")
    void opponentsUpkeepDoesNotRequireSacrifice() {
        harness.addToBattlefield(player1, new TheGitrogMonster());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Gitrog Monster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controller may play one additional land each turn; opponents may not")
    void grantsControllerOnlyExtraLandPlay() {
        harness.addToBattlefield(player1, new TheGitrogMonster());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can actually play two lands in one turn")
    void controllerPlaysTwoLandsInOneTurn() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Forest".equals(p.getCard().getName()))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional land permission does not allow a third land in the same turn")
    void thirdLandPlayIsRejected() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(handSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("A land milled into the graveyard from the library draws a card")
    void landMilledFromLibraryDrawsCard() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        addCreatureReady(player1, new GrizzledAngler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new HauntedDead(), new HauntedDead()));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Gitrog Monster");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(handSize()).isEqualTo(1);
        harness.assertInHand(player1, "Haunted Dead");
    }

    @Test
    @DisplayName("Two lands milled simultaneously draw only one card")
    void simultaneousMilledLandsDrawOnce() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        addCreatureReady(player1, new GrizzledAngler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new HauntedDead(), new HauntedDead()));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(handSize()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Separate land milling events each draw a card")
    void separateMillEventsEachDraw() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        addCreatureReady(player1, new GrizzledAngler());
        addCreatureReady(player1, new GrizzledAngler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new HauntedDead(),
                new Forest(), new Forest(), new HauntedDead(), new HauntedDead()));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(handSize()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Milling nonlands does not draw a card")
    void milledNonlandsDoNotDraw() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        addCreatureReady(player1, new GrizzledAngler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HauntedDead(), new HauntedDead(), new Forest()));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(handSize()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Lands entering an opponent's graveyard do not draw a card")
    void opponentsMilledLandsDoNotDraw() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        addCreatureReady(player2, new GrizzledAngler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HauntedDead()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new HauntedDead()));

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(handSize()).isZero();
    }

    @Test
    @DisplayName("Discarding two lands for one activation cost draws only one card")
    void simultaneousDiscardedLandsDrawOnce() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.setGraveyard(player1, List.of(new HauntedDead()));
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new HauntedDead(), new HauntedDead(), new HauntedDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Haunted Dead");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(handSize()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Losing all abilities removes the additional land play permission")
    void losingAbilitiesRemovesAdditionalLandPlay() {
        var gitrog = harness.addToBattlefieldAndReturn(player1, new TheGitrogMonster());
        harness.setHand(player1, List.of(new ImprisonedInTheMoon(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, gitrog.getId());
        resolveAllTriggers();
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(handSize()).isEqualTo(1);
    }
}
