package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.p.PlatypusBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldOut.class, PlatypusBear.class, ErdwalIlluminator.class})
class SoldOutTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and creates a Clue if it was dealt damage this turn")
    void exilesDamagedCreatureAndCreatesClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castSoldOut(target);

        harness.assertNotOnBattlefield(player2, "Platypus-Bear");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature without creating a Clue if it was not dealt damage this turn")
    void exilesUndamagedCreatureWithoutCreatingClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());

        castSoldOut(target);

        harness.assertNotOnBattlefield(player2, "Platypus-Bear");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @CardUsed(ErdwalIlluminator.class)
    @DisplayName("Creating the Clue does not investigate or trigger Erdwal Illuminator")
    void creatingClueDoesNotInvestigate() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castSoldOut(target);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Can exile its controller's damaged creature and create a Clue")
    void exilesOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlatypusBear());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castSoldOut(target);

        harness.assertNotOnBattlefield(player1, "Platypus-Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Creates no Clue when the damaged target leaves before resolution")
    void missingTargetDoesNotCreateClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new SoldOut()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatypusBear());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setLibrary(player1, List.of(new PlatypusBear()));

        castSoldOut(target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInHand(player1, "Platypus-Bear");
    }

    private void castSoldOut(Permanent target) {
        harness.setHand(player1, List.of(new SoldOut()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
