package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlliedStrategies;
import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.MagmaBurst;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightscapeFamiliar.class, AlliedStrategies.class, MagmaBurst.class, AlphaKavu.class, Terminate.class})
class NightscapeFamiliarTest extends BaseCardTest {

    @Test
    void reducesBlueSpellsYouCast() {
        harness.addToBattlefield(player1, new NightscapeFamiliar());
        harness.setHand(player1, List.of(new AlliedStrategies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesRedSpellsYouCast() {
        harness.addToBattlefield(player1, new NightscapeFamiliar());
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceOtherColorsOrOpponentsSpells() {
        harness.addToBattlefield(player1, new NightscapeFamiliar());
        harness.setHand(player1, List.of(new AlphaKavu()));
        harness.setHand(player2, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityGrantsRegenerationShield() {
        Permanent familiar = addCreatureReady(player1, new NightscapeFamiliar());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(familiar.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void multipleFamiliarsReduceTheSameSpell() {
        harness.addToBattlefield(player1, new NightscapeFamiliar());
        harness.addToBattlefield(player1, new NightscapeFamiliar());
        harness.setHand(player1, List.of(new AlliedStrategies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new NightscapeFamiliar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Kavu");
    }

    @Test
    void regenerationReplacesLethalDamageAndConsumesTheShield() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(familiar.isTapped()).isFalse();
        harness.setHand(player2, List.of(new MagmaBurst(), new MagmaBurst()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nightscape Familiar");
        assertThat(familiar.isTapped()).isTrue();
        assertThat(familiar.getMarkedDamage()).isZero();
        assertThat(familiar.getRegenerationShield()).isZero();

        harness.castInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nightscape Familiar");
    }

    @Test
    void regenerationDoesNotPreventTerminate() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar());
        familiar.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(familiar.getRegenerationShield()).isEqualTo(1);
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nightscape Familiar");
        harness.assertInGraveyard(player1, "Nightscape Familiar");
    }
}
