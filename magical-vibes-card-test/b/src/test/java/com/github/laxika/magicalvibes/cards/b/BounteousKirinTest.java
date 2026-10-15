package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.d.DeathknellKami;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BounteousKirin.class, ArabaMothrider.class, DeathknellKami.class, SpiritualVisit.class})
class BounteousKirinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell may gain life equal to its mana value")
    void arcaneSpellGainsLifeByManaValue() {
        addBounteousKirin();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Casting a Spirit spell may gain life equal to its mana value")
    void spiritSpellGainsLifeByManaValue() {
        addBounteousKirin();
        harness.castFromHand(player1, new DeathknellKami(), "{1}{B}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void decliningDoesNothing() {
        addBounteousKirin();
        harness.castFromHand(player1, new DeathknellKami(), "{1}{B}");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addBounteousKirin();
        harness.castFromHand(player1, new ArabaMothrider(), "{1}{W}");

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent casting a Spirit or Arcane spell does not trigger")
    void opponentSpellDoesNotTrigger() {
        addBounteousKirin();
        harness.castFromHand(player2, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting Kirin itself does not trigger its battlefield ability")
    void castingKirinItselfDoesNotTrigger() {
        harness.castFromHand(player1, new BounteousKirin(), "{5}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bounteous Kirin");
        harness.assertLife(player1, 20);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Spirit entering without being cast does not trigger")
    void spiritEnteringWithoutBeingCastDoesNotTrigger() {
        addBounteousKirin();
        harness.enterBattlefieldAndReturn(player1, new DeathknellKami());

        harness.assertOnBattlefield(player1, "Deathknell Kami");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addBounteousKirin() {
        harness.addToBattlefield(player1, new BounteousKirin());
    }
}
