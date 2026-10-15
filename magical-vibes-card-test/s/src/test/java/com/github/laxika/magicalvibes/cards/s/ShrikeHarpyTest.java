package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrikeHarpy.class, GrizzlyBears.class, GiantSpider.class})
class ShrikeHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and the sacrifice ability does not trigger")
    void opponentPaysTribute() {
        castShrikeHarpy();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shrike Harpy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute makes the targeted opponent choose a creature to sacrifice")
    void opponentDeclinesTribute() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castShrikeHarpy();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, giant.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The ETB ability cannot target its controller")
    void cannotTargetController() {
        castShrikeHarpy();
        harness.handleMayAbilityChosen(player2, false);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying tribute places two counters and leaves the opponent's creature intact")
    void tributePlacesCountersWithoutSacrifice() {
        harness.addToBattlefield(player2, new ShrikeHarpy());

        castShrikeHarpy();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shrike Harpy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Shrike Harpy");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent with no creatures can be targeted and sacrifices nothing")
    void opponentWithoutCreatures() {
        castShrikeHarpy();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shrike Harpy");
        assertThat(findPermanent(player1, "Shrike Harpy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castShrikeHarpy() {
        harness.setHand(player1, List.of(new ShrikeHarpy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
