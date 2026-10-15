package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.p.PowerSink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Channel.class, GrizzlyBears.class, PlatinumEmperion.class, PowerSink.class})
class ChannelTest extends BaseCardTest {

    private void castChannel() {
        harness.setHand(player1, List.of(new Channel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Casting Channel lets its controller pay life for colorless mana")
    void grantsChannelPermission() {
        castChannel();

        assertThat(gd.mayPayLifeForColorlessManaUntilEndOfTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Paying 1 life adds one unrestricted colorless mana")
    void payLifeAddsUnrestrictedColorlessMana() {
        harness.setLife(player1, 20);
        castChannel();

        harness.payLifeForColorlessMana(player1);

        var pool = gd.playerManaPools.get(player1.getId());
        harness.assertLife(player1, 19);
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        // Channel mana is unrestricted, unlike Piracy's spell-only mana
        assertThat(pool.getSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying life for colorless mana can be repeated any number of times")
    void payLifeMultipleTimes() {
        harness.setLife(player1, 20);
        castChannel();

        harness.payLifeForColorlessMana(player1);
        harness.payLifeForColorlessMana(player1);
        harness.payLifeForColorlessMana(player1);

        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Colorless mana from Channel can pay the generic cost of a spell")
    void channelManaCastsASpell() {
        castChannel();
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Grizzly Bears costs {1}{G}: the {1} is paid by Channel's colorless mana
        harness.payLifeForColorlessMana(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot pay life for mana without Channel")
    void cannotPayLifeWithoutChannel() {
        assertThatThrownBy(() -> harness.payLifeForColorlessMana(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay life for mana with less than 1 life")
    void cannotPayLifeWithoutEnoughLife() {
        castChannel();
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.payLifeForColorlessMana(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permission to pay life for mana wears off at end of turn")
    void permissionClearedAtEndOfTurn() {
        castChannel();

        assertThat(gd.mayPayLifeForColorlessManaUntilEndOfTurn).isNotEmpty();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.payLifeForColorlessMana(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayLifeWhenLifeTotalCannotChange() {
        castChannel();
        harness.addToBattlefield(player1, new PlatinumEmperion());

        assertThatThrownBy(() -> harness.payLifeForColorlessMana(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void permissionDoesNotExtendToOpponent() {
        castChannel();

        assertThatThrownBy(() -> harness.payLifeForColorlessMana(player2))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void payingLifeDoesNotUseTheStack() {
        castChannel();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        int stackSize = gd.stack.size();

        harness.payLifeForColorlessMana(player1);

        assertThat(gd.stack).hasSize(stackSize);
        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void canPayLifeDuringResolvingManaPayment() {
        castChannel();
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        gs.payLifeForColorlessMana(gd, player1);

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
