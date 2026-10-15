package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.cards.o.OathswornVampire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForerunnerOfTheLegion.class, OathswornVampire.class, SunSentinel.class})
class ForerunnerOfTheLegionTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a Vampire and put it on top of the library")
    void maySearchForVampireToTopOfLibrary() {
        harness.setHand(player1, List.of(new ForerunnerOfTheLegion()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLibrary(player1, List.of(new OathswornVampire(), new SunSentinel()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .isInstanceOf(OathswornVampire.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(OathswornVampire.class);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another Vampire entering lets the ability boost a target creature")
    void vampireEnteringBoostsTargetCreature() {
        harness.addToBattlefield(player1, new ForerunnerOfTheLegion());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunSentinel());

        harness.setHand(player1, List.of(new OathswornVampire()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Vampire entering does not trigger the boost")
    void nonVampireEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new ForerunnerOfTheLegion());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunSentinel());

        harness.setHand(player1, List.of(new SunSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the search leaves the library unchanged and does not trigger a self boost")
    void canDeclineSearchWithoutSelfTrigger() {
        OathswornVampire vampire = new OathswornVampire();
        SunSentinel sentinel = new SunSentinel();
        harness.setLibrary(player1, List.of(vampire, sentinel));
        harness.setHand(player1, List.of(new ForerunnerOfTheLegion()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vampire, sentinel);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        Permanent forerunner = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, forerunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forerunner)).isEqualTo(2);
    }

    @Test
    @DisplayName("Searching a library with no Vampire completes without moving a card")
    void searchWithoutMatchingCardCompletes() {
        SunSentinel sentinel = new SunSentinel();
        harness.setLibrary(player1, List.of(sentinel));
        harness.setHand(player1, List.of(new ForerunnerOfTheLegion()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sentinel);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A restricted search can fail to find even with a Vampire in the library")
    void canFailToFindMatchingVampire() {
        OathswornVampire vampire = new OathswornVampire();
        harness.setLibrary(player1, List.of(vampire));
        harness.setHand(player1, List.of(new ForerunnerOfTheLegion()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vampire);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Vampire entering does not trigger the boost")
    void opponentsVampireDoesNotTrigger() {
        harness.addToBattlefield(player1, new ForerunnerOfTheLegion());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new OathswornVampire()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The boost may target an opponent's non-Vampire and expires at end of turn")
    void canBoostOpponentCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new ForerunnerOfTheLegion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunSentinel());
        harness.setHand(player1, List.of(new OathswornVampire()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The entering Vampire can itself be the boost target")
    void canTargetEnteringVampire() {
        harness.addToBattlefield(player1, new ForerunnerOfTheLegion());
        OathswornVampire vampire = new OathswornVampire();
        harness.setHand(player1, List.of(vampire));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == vampire)
                .findFirst().orElseThrow();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
