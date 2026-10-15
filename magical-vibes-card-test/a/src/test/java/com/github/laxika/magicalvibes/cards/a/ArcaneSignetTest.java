package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneSignet.class, AtraxaPraetorsVoice.class})
class ArcaneSignetTest extends BaseCardTest {

    @Test
    void addsOneManaOfChosenColorAndTaps() {
        gd.playerCommanders.put(player1.getId(), List.of(new AtraxaPraetorsVoice()));
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(signet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void offersOnlyColorsInControllersCommanderIdentity() {
        gd.playerCommanders.put(player1.getId(), List.of(new AtraxaPraetorsVoice()));
        harness.addToBattlefield(player1, new ArcaneSignet());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "GREEN");

        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsColorOutsideCommanderIdentity() {
        gd.playerCommanders.put(player1.getId(), List.of(new AtraxaPraetorsVoice()));
        harness.addToBattlefield(player1, new ArcaneSignet());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "RED"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.handleListChoice(player1, "WHITE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void producesNoManaWithoutCommanderEvenWhenOpponentHasOne() {
        gd.playerCommanders.put(player1.getId(), List.of());
        gd.playerCommanders.put(player2.getId(), List.of(new AtraxaPraetorsVoice()));
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());

        harness.activateAbility(player1, 0, null, null);

        assertThat(signet.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
    }
}
