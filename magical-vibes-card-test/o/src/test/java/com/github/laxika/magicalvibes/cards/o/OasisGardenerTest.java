package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OasisGardener.class})
class OasisGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains its controller 2 life")
    void entersAndGainsLife() {
        harness.setHand(player1, List.of(new OasisGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
    }

    @Test
    @DisplayName("Tapping Oasis Gardener prompts for a color and adds one mana")
    void tapsForAnyColorMana() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new OasisGardener());
        gardener.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gardener.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Each color choice adds exactly one mana without using the stack")
    void producesEachColorImmediately(ManaColor color) {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new OasisGardener());
        gardener.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gardener.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("Summoning sickness prevents tapping for mana")
    void cannotTapWhileSummoningSick() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new OasisGardener());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gardener.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Gardener cannot produce more mana")
    void cannotTapAgain() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new OasisGardener());
        gardener.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry trigger uses the stack and survives its source leaving")
    void entryTriggerSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new OasisGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, startingLife);
        assertThat(gd.stack).hasSize(1);
        Permanent gardener = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(gardener);
        gd.playerGraveyards.get(player1.getId()).add(gardener.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        harness.assertLife(player2, opponentLife);
    }
}
