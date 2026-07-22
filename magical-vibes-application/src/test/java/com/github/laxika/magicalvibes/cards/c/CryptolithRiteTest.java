package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptolithRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain tap ability to add one mana of any color")
    void ownCreaturesGainAnyColorManaAbility() {
        harness.addToBattlefield(player1, new CryptolithRite());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        harness.activateAbility(player1, bearsIndex, null, null);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures do not have the granted ability without Cryptolith Rite")
    void creaturesDoNotHaveAbilityWithoutCryptolithRite() {
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponent creatures do not gain Cryptolith Rite ability")
    void opponentCreaturesDoNotGainAbility() {
        harness.addToBattlefield(player1, new CryptolithRite());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Lands do not gain Cryptolith Rite ability")
    void landsDoNotGainAbility() {
        harness.addToBattlefield(player1, new CryptolithRite());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);

        // Forest already has its printed mana ability at index 0; a wrongly granted second ability
        // would live at index 1.
        assertThatThrownBy(() -> harness.activateAbility(player1, forestIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted ability is lost when Cryptolith Rite leaves battlefield")
    void grantedAbilityLostWhenCryptolithRiteLeaves() {
        harness.addToBattlefield(player1, new CryptolithRite());
        addCreatureReady(player1, new GrizzlyBears());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Cryptolith Rite"));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
