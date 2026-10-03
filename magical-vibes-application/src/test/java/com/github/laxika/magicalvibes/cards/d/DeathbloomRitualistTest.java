package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({DeathbloomRitualist.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class DeathbloomRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of the chosen color for each creature card in its controller's graveyard")
    void addsManaPerCreatureCardInControllersGraveyard() {
        Permanent ritualist = addReadyRitualist();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(ritualist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ignores noncreature cards and cards in an opponent's graveyard")
    void ignoresNoncreatureAndOpponentGraveyardCards() {
        addReadyRitualist();
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addReadyRitualist() {
        return addCreatureReady(player1, new DeathbloomRitualist());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesEntireAmountInOneChosenColor(ManaColor color) {
        addReadyRitualist();
        harness.setGraveyard(player1, List.of(new DeathbloomRitualist(), new DeathbloomRitualist()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 2 : 0);
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyGraveyardAddsNoManaButStillPaysTapCost() {
        Permanent ritualist = addReadyRitualist();
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(ritualist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void summoningSickRitualistCannotActivate() {
        Permanent ritualist = harness.addToBattlefieldAndReturn(player1, new DeathbloomRitualist());
        ritualist.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new DeathbloomRitualist()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ritualist.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tappedRitualistCannotActivateAgain() {
        Permanent ritualist = addReadyRitualist();
        harness.setGraveyard(player1, List.of(new DeathbloomRitualist()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ritualist.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
