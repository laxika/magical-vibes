package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuardDuty;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampartArchitect.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class, GuardDuty.class})
class RampartArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/3 Wall with defender")
    void enteringCreatesWall() {
        setupArchitectAndResolveEtb();

        Permanent wall = findPermanents(player1, "Wall").getFirst();
        assertThat(wall.getCard().getSubtypes()).contains(CardSubtype.WALL);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(wall.getEffectivePower()).isEqualTo(1);
        assertThat(wall.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking creates another Wall")
    void attackingCreatesWall() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new RampartArchitect());
        architect.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wall")).hasSize(1);
    }

    @Test
    @DisplayName("A dying defender offers a tapped basic land search")
    void dyingDefenderOffersBasicLandSearch() {
        setupArchitectAndResolveEtb();
        Permanent wall = findPermanents(player1, "Wall").getFirst();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), wall));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Forest", "Island");
        String chosenName = offered.getFirst().getName();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = findPermanent(player1, chosenName);
        assertThat(chosenLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A dying creature without defender does not trigger the search")
    void dyingNonDefenderDoesNotTrigger() {
        harness.addToBattlefield(player1, new RampartArchitect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), bears));
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    private void setupArchitectAndResolveEtb() {
        harness.castFromHand(player1, new RampartArchitect(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void mayDeclineDefenderDeathSearch() {
        setupArchitectAndResolveEtb();
        Permanent wall = findPermanents(player1, "Wall").getFirst();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wall));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void opposingDefenderDeathDoesNotTriggerSearch() {
        harness.addToBattlefield(player1, new RampartArchitect());
        harness.enterBattlefieldAndReturn(player2, new RampartArchitect());
        harness.passBothPriorities();
        Permanent wall = findPermanents(player2, "Wall").getFirst();
        Permanent opposingArchitect = findPermanent(player2, "Rampart Architect");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opposingArchitect));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wall));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void architectWithDefenderTriggersForItsOwnDeath() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new RampartArchitect());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, architect.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, architect));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }
}
