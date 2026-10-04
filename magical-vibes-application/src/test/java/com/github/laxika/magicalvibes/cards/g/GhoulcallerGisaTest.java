package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulcallerGisa.class, GrizzlyBears.class})
class GhoulcallerGisaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Zombie for each power of the sacrificed creature")
    void createsZombiesEqualToSacrificedPower() {
        addCreatureReady(player1, new GhoulcallerGisa());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective power")
    void usesEffectivePower() {
        addCreatureReady(player1, new GhoulcallerGisa());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(3);
    }

    @Test
    @DisplayName("Cannot sacrifice Gisa herself")
    void cannotSacrificeSource() {
        addCreatureReady(player1, new GhoulcallerGisa());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysCostsBeforeCreatingUntappedBlackZombieTokens() {
        Permanent gisa = addCreatureReady(player1, new GhoulcallerGisa());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gisa.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Zombie")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    void usesPowerOfChosenCreatureWhenMultipleCreaturesAreAvailable() {
        addCreatureReady(player1, new GhoulcallerGisa());
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(4);
        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(unchosen);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void zeroPowerCreatesNoTokensButStillPaysCosts() {
        Permanent gisa = addCreatureReady(player1, new GhoulcallerGisa());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(-2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gisa.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void negativePowerCreatesNoTokens() {
        addCreatureReady(player1, new GhoulcallerGisa());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(-3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new GhoulcallerGisa());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent gisa = addCreatureReady(player1, new GhoulcallerGisa());
        gisa.tap();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent gisa = addCreatureReady(player1, new GhoulcallerGisa());
        gisa.setSummoningSick(true);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        addCreatureReady(player1, new GhoulcallerGisa());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
