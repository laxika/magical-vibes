package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynodSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T}: Exile target permanent you control, tracked with Synod Sanctum")
    void exileAbilityExilesOwnPermanent() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("{2}, {T} cannot target a permanent you don't control")
    void exileAbilityCannotTargetOpponentPermanent() {
        harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{2}, Sacrifice: return cards exiled with Synod Sanctum under your control")
    void sacrificeReturnsExiledPermanents() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synod Sanctum");
        harness.assertInGraveyard(player1, "Synod Sanctum");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).isEmpty();
    }
}
