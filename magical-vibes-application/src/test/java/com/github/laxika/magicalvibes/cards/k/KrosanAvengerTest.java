package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KrosanAvenger.class)
class KrosanAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Can regenerate with seven or more cards in its controller's graveyard")
    void canRegenerateWithThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new KrosanAvenger());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(avenger.getRegenerationShield()).isEqualTo(1);
        assertThat(avenger.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate regeneration with fewer than seven cards in its graveyard")
    void cannotRegenerateBelowThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.addToBattlefield(player1, new KrosanAvenger());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new KrosanAvenger());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }

    @Test
    @DisplayName("Cannot pay the regeneration cost without green mana")
    void cannotRegenerateWithoutGreenMana() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new KrosanAvenger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal damage and is spent")
    void regenerationShieldSavesFromLethalDamage() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent avenger = addCreatureReady(player1, new KrosanAvenger());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        avenger.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avenger);
        assertThat(avenger.getRegenerationShield()).isZero();
        assertThat(avenger.isTapped()).isTrue();
        assertThat(avenger.getMarkedDamage()).isZero();
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new KrosanAvenger(), new KrosanAvenger(), new KrosanAvenger(), new KrosanAvenger(),
                new KrosanAvenger(), new KrosanAvenger(), new KrosanAvenger());
    }
}
