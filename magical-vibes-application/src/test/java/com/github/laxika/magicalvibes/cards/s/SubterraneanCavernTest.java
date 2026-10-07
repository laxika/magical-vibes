package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SubterraneanCavern.class})
class SubterraneanCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SubterraneanCavern()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability prompts for black or green")
    void manaAbilityPromptsForBlackOrGreen() {
        addReadyCavern(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "GREEN");
    }

    @Test
    @DisplayName("Choosing black adds one black mana")
    void choosingBlackAddsMana() {
        Permanent cavern = addReadyCavern(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing green adds one green mana")
    void choosingGreenAddsMana() {
        Permanent cavern = addReadyCavern(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();
    }

    private Permanent addReadyCavern(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SubterraneanCavern());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Life gain waits for the entry trigger to resolve")
    void lifeGainUsesStackAndOnlyBenefitsController() {
        harness.setHand(player1, List.of(new SubterraneanCavern()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped cavern cannot activate its mana ability")
    void tappedCavernCannotProduceMana() {
        Permanent cavern = addReadyCavern(player1);
        cavern.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An untapped noncreature cavern can produce mana the turn it enters")
    void noncreatureLandCanProduceManaWhileSummoningSick() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new SubterraneanCavern());
        cavern.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(cavern.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
