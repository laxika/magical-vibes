package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.cards.z.ZealousInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArkOfBlight.class, TempleOfTheFalseGod.class, ZealousInquisitor.class})
class ArkOfBlightTest extends BaseCardTest {

    @Test
    @DisplayName("Can target its controller's land and pays costs before resolution")
    void canDestroyOwnLandAndPaysCostsImmediately() {
        harness.addToBattlefield(player1, new ArkOfBlight());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, land.getId());

        harness.assertNotOnBattlefield(player1, "Ark of Blight");
        harness.assertInGraveyard(player1, "Ark of Blight");
        harness.assertOnBattlefield(player1, "Temple of the False God");
        harness.assertNotInGraveyard(player1, "Temple of the False God");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Temple of the False God");
        harness.assertInGraveyard(player1, "Temple of the False God");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice costs remain paid when the target is destroyed in response")
    void targetDestroyedInResponseDoesNotRefundSacrifice() {
        harness.addToBattlefield(player1, new ArkOfBlight());
        harness.addToBattlefield(player1, new ArkOfBlight());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.activateAbility(player1, 0, null, land.getId());
        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Ark of Blight");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Temple of the False God");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Ark of Blight"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Temple of the False God");
    }

    @Test
    @DisplayName("Pays three mana, sacrifices itself, and destroys a target land")
    void sacrificesAndDestroysTargetLand() {
        harness.addToBattlefield(player1, new ArkOfBlight());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ark of Blight");
        harness.assertInGraveyard(player2, "Temple of the False God");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new ArkOfBlight());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZealousInquisitor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires three mana to activate")
    void cannotActivateWithoutThreeMana() {
        harness.addToBattlefield(player1, new ArkOfBlight());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ark of Blight");
        harness.assertOnBattlefield(player2, "Temple of the False God");
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent ark = harness.addToBattlefieldAndReturn(player1, new ArkOfBlight());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        ark.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ark of Blight");
        harness.assertOnBattlefield(player2, "Temple of the False God");
    }
}
