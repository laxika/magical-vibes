package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoinerAdept.class, Forest.class, GrizzlyBears.class})
class JoinerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control gain tap ability to add one mana of any color")
    void ownLandsGainAnyColorManaAbility() {
        harness.addToBattlefield(player1, new JoinerAdept());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);

        harness.activateAbility(player1, forestIndex, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonland permanents do not gain Joiner Adept ability")
    void nonLandsDoNotGainAbility() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Lands do not have the granted ability without Joiner Adept")
    void landsDoNotHaveAbilityWithoutJoinerAdept() {
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponent lands do not gain Joiner Adept ability")
    void opponentLandsDoNotGainAbility() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Granted ability is lost when Joiner Adept leaves battlefield")
    void grantedAbilityLostWhenJoinerAdeptLeaves() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new Forest());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Joiner Adept"));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Granted ability produces exactly one mana of the chosen color")
    void producesEachColor(ManaColor color) {
        harness.addToBattlefield(player1, new JoinerAdept());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("Forest retains its original mana ability")
    void retainsOriginalManaAbility() {
        harness.addToBattlefield(player1, new JoinerAdept());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One remaining Joiner Adept continues granting the ability")
    void abilityRemainsWithAnotherAdept() {
        Permanent firstAdept = harness.addToBattlefieldAndReturn(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new Forest());
        gd.playerBattlefields.get(player1.getId()).remove(firstAdept);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land cannot activate the granted ability again")
    void tappedLandCannotActivateAgain() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
