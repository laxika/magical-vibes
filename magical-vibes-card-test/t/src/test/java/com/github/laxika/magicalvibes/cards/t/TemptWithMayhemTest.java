package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemptWithMayhem.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class TemptWithMayhemTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a controller copy and rewards an accepting opponent with another controller copy")
    void acceptingOpponentCreatesCopiesForBothPlayers() {
        CounselOfTheSoratami counsel = castCounsel();

        castTemptWithMayhem(counsel);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3);
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player1.getId())).hasSize(2);
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant an additional copy")
    void decliningOpponentLeavesOnlyBaseCopy() {
        CounselOfTheSoratami counsel = castCounsel();

        castTemptWithMayhem(counsel);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.setHand(player1, List.of(new TemptWithMayhem()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller copies are created only after opponents finish deciding")
    void noControllerCopyBeforeOpponentChoice() {
        CounselOfTheSoratami counsel = castCounsel();

        castTemptWithMayhem(counsel);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).isEmpty();
    }

    @Test
    @DisplayName("All controller copies are above the opponent copy on the stack")
    void controllerCopiesResolveBeforeOpponentCopy() {
        CounselOfTheSoratami counsel = castCounsel();

        castTemptWithMayhem(counsel);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)
                .map(StackEntry::getControllerId).toList())
                .containsExactly(player2.getId(), player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("Copies resolve for their respective controllers without paying mana")
    void copiesDrawCardsForTheirControllers() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());
        CounselOfTheSoratami counsel = castCounsel();
        castTemptWithMayhem(counsel);
        harness.handleMayAbilityChosen(player2, true);

        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player1, "Tempt with Mayhem");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller can choose a new target for a copied instant")
    void copiedInstantCanChooseNewTarget() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        castTemptWithMayhem(shock);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An accepting opponent chooses new targets before the controller does")
    void opponentRetargetsBeforeController() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        castTemptWithMayhem(shock);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player2.getId()))
                .extracting(StackEntry::getTargetId).containsExactly(player1.getId());
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player1.getId()))
                .extracting(StackEntry::getTargetId).containsExactly(player2.getId(), player2.getId());
    }

    private CounselOfTheSoratami castCounsel() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0);
        return counsel;
    }

    private void castTemptWithMayhem(Card spell) {
        harness.setHand(player1, List.of(new TemptWithMayhem()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, spell.getId());
    }
}
