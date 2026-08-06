package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColdStorageTest extends BaseCardTest {

    @Test
    @DisplayName("{3}: Exile target creature you control, tracked with Cold Storage")
    void exileAbilityExilesOwnCreature() {
        Permanent storage = harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(storage.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("{3} cannot target a creature you don't control")
    void exileAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice: return each creature exiled with Cold Storage under your control")
    void sacrificeReturnsExiledCreatures() {
        Permanent storage = harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(storage.getId())).hasSize(1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cold Storage");
        harness.assertInGraveyard(player1, "Cold Storage");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(storage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice with no exiled cards still sacrifices the artifact")
    void sacrificeWithNoExiledCards() {
        harness.addToBattlefieldAndReturn(player1, new ColdStorage());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cold Storage");
        harness.assertInGraveyard(player1, "Cold Storage");
    }
}
