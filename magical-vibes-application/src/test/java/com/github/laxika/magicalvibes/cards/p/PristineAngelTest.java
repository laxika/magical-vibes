package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.cards.m.Memnarch;
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

@CardUsed({PristineAngel.class, EchoingDecay.class, Memnarch.class, DarksteelIngot.class})
class PristineAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Pristine Angel has protection from black")
    void untappedAngelHasProtectionFromBlack() {
        Permanent angel = addAngel(player2);
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Untapped Pristine Angel has protection from artifacts")
    void untappedAngelHasProtectionFromArtifacts() {
        Permanent angel = addAngel(player2);
        harness.addToBattlefield(player1, new Memnarch());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);

        angel.tap();
        harness.activateAbility(player1, 0, 0, null, angel.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Tapped Pristine Angel can be targeted by a colored spell")
    void tappedAngelLosesProtectionFromColors() {
        Permanent angel = addAngel(player2);
        angel.tap();
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, angel.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a spell may untap Pristine Angel")
    void castingSpellMayUntapAngel() {
        Permanent angel = addAngel(player1);
        angel.tap();
        harness.setHand(player1, List.of(new DarksteelIngot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining Pristine Angel's trigger leaves it tapped")
    void decliningUntapLeavesAngelTapped() {
        Permanent angel = addAngel(player1);
        angel.tap();
        harness.setHand(player1, List.of(new DarksteelIngot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(angel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's spell does not trigger Pristine Angel's untap ability")
    void opponentSpellDoesNotTriggerUntapAbility() {
        Permanent angel = addAngel(player1);
        angel.tap();
        harness.setHand(player2, List.of(new DarksteelIngot()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castArtifact(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(angel.isTapped()).isTrue();
    }

    private Permanent addAngel(Player player) {
        return addCreatureReady(player, new PristineAngel());
    }
}
