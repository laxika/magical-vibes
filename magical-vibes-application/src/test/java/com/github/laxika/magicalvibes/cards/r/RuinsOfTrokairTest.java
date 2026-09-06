package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuinsOfTrokair.class})
class RuinsOfTrokairTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RuinsOfTrokair()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Ruins of Trokair").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one white mana")
    void tapAddsOneWhiteMana() {
        harness.addToBattlefield(player1, new RuinsOfTrokair());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Ruins of Trokair").isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Ruins of Trokair");
    }

    @Test
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void cannotActivateManaAbilitiesWhileTapped() {
        Permanent ruins = harness.addToBattlefieldAndReturn(player1, new RuinsOfTrokair());
        ruins.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertOnBattlefield(player1, "Ruins of Trokair");
    }

    @Test
    @DisplayName("Tap and sacrifice adds two white mana and moves the land to the graveyard")
    void sacrificeAddsTwoWhiteMana() {
        harness.addToBattlefield(player1, new RuinsOfTrokair());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Ruins of Trokair");
        harness.assertInGraveyard(player1, "Ruins of Trokair");
    }
}
