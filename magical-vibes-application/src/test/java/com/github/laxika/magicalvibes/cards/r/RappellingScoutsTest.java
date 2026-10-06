package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CinderElemental;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RappellingScouts.class, CinderElemental.class})
class RappellingScoutsTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants protection from the chosen color until end of turn")
    void grantsProtectionFromChosenColor() {
        Permanent scouts = addCreatureReady(player1, new RappellingScouts());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Chosen-color protection stops an ability of that color from targeting it")
    void protectionStopsRedAbility() {
        Permanent scouts = addCreatureReady(player1, new RappellingScouts());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        addCreatureReady(player2, new CinderElemental());
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, scouts.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent scouts = addCreatureReady(player1, new RappellingScouts());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations retain protection from both chosen colors and affect only the source")
    void repeatedActivationsProtectOnlyTheirSource() {
        Permanent scouts = addCreatureReady(player1, new RappellingScouts());
        Permanent otherScouts = addCreatureReady(player1, new RappellingScouts());
        Permanent opposingScouts = addCreatureReady(player2, new RappellingScouts());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.WHITE)).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherScouts, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherScouts, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opposingScouts, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opposingScouts, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection gained in response makes a red damage ability's target illegal")
    void protectionInResponseStopsDamageAbility() {
        Permanent scouts = addCreatureReady(player1, new RappellingScouts());
        addCreatureReady(player2, new CinderElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, 4, scouts.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rappelling Scouts");
        assertThat(scouts.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Cinder Elemental");
    }

    @Test
    @DisplayName("The protection ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent scouts = addCreatureReady(player1, new RappellingScouts());
        scouts.tap();
        scouts.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, scouts, CardColor.GREEN)).isTrue();
        assertThat(scouts.isTapped()).isTrue();
    }
}
