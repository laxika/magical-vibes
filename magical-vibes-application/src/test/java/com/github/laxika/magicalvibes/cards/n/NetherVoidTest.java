package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetherVoid.class, DurkwoodBoars.class})
class NetherVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its caster cannot pay {3}")
    void countersSpellWhenCasterCannotPay() {
        harness.addToBattlefield(player1, new NetherVoid());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DurkwoodBoars(), "{4}{G}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Counters a spell when its caster declines to pay {3}")
    void countersSpellWhenCasterDeclinesToPay() {
        harness.addToBattlefield(player1, new NetherVoid());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new DurkwoodBoars(), "{4}{G}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Lets a spell resolve when its caster pays {3}")
    void letsSpellResolveWhenCasterPays() {
        harness.addToBattlefield(player1, new NetherVoid());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new DurkwoodBoars(), "{4}{G}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Triggers for a spell cast by its controller")
    void triggersForSpellCastByController() {
        harness.addToBattlefield(player1, new NetherVoid());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player1, new DurkwoodBoars(), "{4}{G}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Durkwood Boars");
    }
}
