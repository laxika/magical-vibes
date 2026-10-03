package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DebtToTheKami.class, GrizzlyBears.class, GloriousAnthem.class, JukaiPreserver.class})
class DebtToTheKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature chosen by the targeted opponent")
    void exilesCreatureMode() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles an enchantment chosen by the targeted opponent")
    void exilesEnchantmentMode() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(1);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
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

    @Test
    @DisplayName("The opponent chooses among enchantments without choosing creatures or the caster's permanents")
    void targetedOpponentChoosesEnchantment() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        cast(1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()))
                .noneMatch(permanent -> permanent.getId().equals(second.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownEnchantment.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(second.getCard().getId()));
        harness.assertNotInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode resolves without exiling anything when the opponent has no matching permanent")
    void noMatchingPermanent(int mode) {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent otherType = harness.addToBattlefieldAndReturn(player2,
                mode == 0 ? new GloriousAnthem() : new GrizzlyBears());

        cast(mode);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).containsExactly(otherType.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(ownCreature.getId(), ownEnchantment.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Debt to the Kami");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("An enchantment creature is eligible for either mode and is exiled rather than dying")
    void eitherModeExilesEnchantmentCreature(int mode) {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JukaiPreserver());

        cast(mode);

        harness.assertNotOnBattlefield(player2, "Jukai Preserver");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        harness.assertNotInGraveyard(player2, "Jukai Preserver");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
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
