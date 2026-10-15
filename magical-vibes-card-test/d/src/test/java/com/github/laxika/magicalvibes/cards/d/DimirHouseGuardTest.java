package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.ClutchOfTheUndercity;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariThug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DimirHouseGuard.class, BorosRecruit.class, DimirSignet.class, GolgariThug.class,
        GlassGolem.class, ClutchOfTheUndercity.class})
class DimirHouseGuardTest extends BaseCardTest {

    @Test
    void transmuteSearchesForTheSameManaValue() {
        DimirHouseGuard houseGuard = new DimirHouseGuard();
        DimirHouseGuard matchingCard = new DimirHouseGuard();
        DimirSignet differentManaValue = new DimirSignet();
        harness.setHand(player1, List.of(houseGuard));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Dimir House Guard");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void sacrificingACreatureRegeneratesDimirHouseGuard() {
        Permanent houseGuard = addCreatureReady(player1, new DimirHouseGuard());
        Permanent fodder = addCreatureReady(player1, new BorosRecruit());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(houseGuard.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Boros Recruit");
    }

    @Test
    void sacrificeIsPaidBeforeTheRegenerationAbilityResolves() {
        Permanent houseGuard = addCreatureReady(player1, new DimirHouseGuard());
        Permanent fodder = addCreatureReady(player1, new BorosRecruit());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        assertThat(houseGuard.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        houseGuard.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Dimir House Guard");
        assertThat(houseGuard.isTapped()).isTrue();
        assertThat(houseGuard.getMarkedDamage()).isZero();
        assertThat(houseGuard.getRegenerationShield()).isZero();
    }

    @Test
    void canSacrificeItselfButCannotRegenerateFromTheGraveyard() {
        Permanent houseGuard = addCreatureReady(player1, new DimirHouseGuard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dimir House Guard");
        harness.assertNotOnBattlefield(player1, "Dimir House Guard");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transmuteMayFailToFindEvenWithAMatchingCard() {
        DimirHouseGuard houseGuard = new DimirHouseGuard();
        DimirHouseGuard matchingCard = new DimirHouseGuard();
        harness.setHand(player1, List.of(houseGuard));
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Dimir House Guard");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transmuteFindsNoncreatureCardsAndExcludesHigherAndLowerManaValues() {
        ClutchOfTheUndercity matchingCard = new ClutchOfTheUndercity();
        harness.setHand(player1, List.of(new DimirHouseGuard()));
        harness.setLibrary(player1, List.of(new DimirSignet(), matchingCard, new GlassGolem()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).doesNotContain(matchingCard);
    }

    @Test
    void transmuteCannotBeActivatedInResponseToAnotherAbility() {
        DimirHouseGuard houseGuard = new DimirHouseGuard();
        harness.setHand(player1, List.of(houseGuard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addCreatureReady(player1, new DimirHouseGuard());
        Permanent fodder = addCreatureReady(player1, new BorosRecruit());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(houseGuard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transmute can only be activated at sorcery speed")
    void transmuteRequiresSorcerySpeed() {
        DimirHouseGuard houseGuard = new DimirHouseGuard();
        harness.setHand(player1, List.of(houseGuard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(houseGuard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void fearPreventsNonblackNonartifactCreatureFromBlocking() {
        addCreatureReady(player1, new DimirHouseGuard());
        addCreatureReady(player2, new BorosRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        addCreatureReady(player1, new DimirHouseGuard());
        addCreatureReady(player2, new GolgariThug());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        addCreatureReady(player1, new DimirHouseGuard());
        addCreatureReady(player2, new GlassGolem());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }
}
