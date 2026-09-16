package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DustBowl.class, Forest.class, FreshVolunteers.class})
class DustBowlTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new DustBowl());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Destroys target nonbasic land without sacrificing Dust Bowl")
    void destroysNonbasicLand() {
        harness.addToBattlefield(player1, new DustBowl());
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new DustBowl());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID targetId = harness.getPermanentId(player2, "Dust Bowl");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.handlePermanentChosen(player1, sacrificedLand.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dust Bowl");
        harness.assertInGraveyard(player2, "Dust Bowl");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Dust Bowl");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land its own controller controls")
    void destroysOwnNonbasicLand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DustBowl());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DustBowl());
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, sacrificedLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(source.getId())
                .doesNotContain(target.getId());
        harness.assertInGraveyard(player1, "Dust Bowl");
    }

    @Test
    @DisplayName("Can sacrifice Dust Bowl itself as the land cost")
    void canSacrificeSourceAsLandCost() {
        harness.addToBattlefield(player1, new DustBowl());
        harness.addToBattlefield(player2, new DustBowl());
        UUID targetId = harness.getPermanentId(player2, "Dust Bowl");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertNotOnBattlefield(player1, "Dust Bowl");
        harness.assertInGraveyard(player1, "Dust Bowl");
        harness.assertOnBattlefield(player2, "Dust Bowl");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dust Bowl");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new DustBowl());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new DustBowl());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID targetId = harness.getPermanentId(player2, "Fresh Volunteers");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying three mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DustBowl());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new DustBowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Dust Bowl");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the destruction ability while Dust Bowl is tapped")
    void cannotActivateWhenTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DustBowl());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new DustBowl());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        source.tap();
        UUID targetId = harness.getPermanentId(player2, "Dust Bowl");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
