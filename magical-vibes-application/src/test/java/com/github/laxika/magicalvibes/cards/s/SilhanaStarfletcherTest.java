package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TorchDrake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({SilhanaStarfletcher.class, TorchDrake.class})
class SilhanaStarfletcherTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Silhana Starfletcher block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new TorchDrake());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SilhanaStarfletcher());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Entering the battlefield asks its controller to choose a color")
    void entersAskingForColor() {
        harness.castFromHand(player1, new SilhanaStarfletcher(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(findPermanent(player1, "Silhana Starfletcher").getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The tap ability adds one mana of the chosen color")
    void addsChosenColorMana() {
        Permanent starfletcher = addCreatureReady(player1, new SilhanaStarfletcher());
        starfletcher.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(starfletcher.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each color can be chosen on entry and produces exactly one mana immediately")
    void producesColorChosenOnEntry(CardColor color) {
        harness.castFromHand(player1, new SilhanaStarfletcher(), "{2}{G}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());
        Permanent starfletcher = findPermanent(player1, "Silhana Starfletcher");
        starfletcher.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(starfletcher.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A newly entered Starfletcher cannot tap for mana while summoning sick")
    void cannotProduceManaWhileSummoningSick() {
        harness.castFromHand(player1, new SilhanaStarfletcher(), "{2}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(findPermanent(player1, "Silhana Starfletcher").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A tapped Starfletcher cannot produce mana again")
    void cannotProduceManaTwiceWithoutUntapping() {
        Permanent starfletcher = addCreatureReady(player1, new SilhanaStarfletcher());
        starfletcher.setChosenColor(CardColor.GREEN);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
