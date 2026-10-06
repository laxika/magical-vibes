package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.i.InnkeepersTalent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PawpatchFormation.class, PlumecreedEscort.class, InnkeepersTalent.class, BakersbaneDuo.class})
class PawpatchFormationTest extends BaseCardTest {

    @Test
    void destroysTargetFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Plumecreed Escort");
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InnkeepersTalent());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Innkeeper's Talent");
    }

    @Test
    void drawsCardAndCreatesFood() {
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bakersbane Duo");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void flyingCreatureModeRejectsNonFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BakersbaneDuo());
        harness.addToBattlefield(player1, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentModeRejectsFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlumecreedEscort());
        harness.addToBattlefield(player2, new InnkeepersTalent());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureModeRejectsEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InnkeepersTalent());
        harness.addToBattlefield(player2, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyControllersFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plumecreed Escort");
    }

    @Test
    void doesNotDestroyCreatureThatLosesFlyingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 0, target.getId());
        target.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plumecreed Escort");
        harness.assertNotInGraveyard(player2, "Plumecreed Escort");
        harness.assertInGraveyard(player1, "Pawpatch Formation");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void foodCanBeSacrificedImmediatelyForThreeLife() {
        createFood();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    void foodActivationRequiresTwoMana() {
        createFood();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @Test
    void tappedFoodCannotBeActivated() {
        createFood();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    private void createFood() {
        harness.setLibrary(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo()));
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
