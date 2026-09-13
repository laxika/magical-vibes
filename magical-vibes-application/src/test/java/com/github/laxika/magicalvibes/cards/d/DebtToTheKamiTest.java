package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DebtToTheKami.class, GrizzlyBears.class, GloriousAnthem.class})
class DebtToTheKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature chosen by the targeted opponent")
    void exilesCreatureMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles an enchantment chosen by the targeted opponent")
    void exilesEnchantmentMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Lets the targeted opponent choose among multiple eligible permanents")
    void targetedOpponentChoosesCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new DebtToTheKami()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new DebtToTheKami()));
        addMana();
        harness.castInstant(player1, 0, mode, player2.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
    }
}
