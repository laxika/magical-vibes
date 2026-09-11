package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BirdsOfParadise.class, GrizzlyBears.class})
class BirdsOfParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Birds of Paradise while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BirdsOfParadise());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Activating Birds of Paradise prompts for mana color immediately")
    void activateAbilityPromptsManaColorImmediately() {
        Permanent birds = addCreatureReady(player1, new BirdsOfParadise());

        harness.activateAbility(player1, 0, null, null);

        assertThat(birds.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    }

    @ParameterizedTest(name = "Choosing {0} adds exactly one mana of that color")
    @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana(ManaColor manaColor) {
        Permanent birds = addCreatureReady(player1, new BirdsOfParadise());
        var manaPool = gd.playerManaPools.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        int before = manaPool.get(manaColor);
        int totalBefore = manaPool.getTotalAllMana();

        harness.handleListChoice(player1, manaColor.name());

        assertThat(manaPool.get(manaColor)).isEqualTo(before + 1);
        assertThat(manaPool.getTotalAllMana()).isEqualTo(totalBefore + 1);
        assertThat(birds.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseColorlessMana() {
        addCreatureReady(player1, new BirdsOfParadise());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot activate Birds of Paradise when it is already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new BirdsOfParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Birds of Paradise")
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new BirdsOfParadise());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
