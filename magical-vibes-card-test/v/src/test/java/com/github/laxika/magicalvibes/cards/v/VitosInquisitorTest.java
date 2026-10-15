package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.g.GlowcapLantern;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitosInquisitor.class, ArmoredKincaller.class, GlowcapLantern.class})
class VitosInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Vito's Inquisitor and grants menace")
    void sacrificingAnotherCreaturePutsCounterAndGrantsMenace() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        harness.addToBattlefield(player1, new ArmoredKincaller());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Armored Kincaller");
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a counter on Vito's Inquisitor")
    void sacrificingAnotherArtifactPutsCounter() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        harness.addToBattlefield(player1, new GlowcapLantern());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Glowcap Lantern");
    }

    @Test
    @DisplayName("Menace wears off at the end of the turn while the counter remains")
    void menaceWearsOffAtEndOfTurn() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        harness.addToBattlefield(player1, new ArmoredKincaller());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Vito's Inquisitor itself")
    void activatedAbilityRequiresAnotherPermanent() {
        addCreatureReady(player1, new VitosInquisitor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The sacrifice is paid before the counter and menace resolve")
    void sacrificeIsAnActivationCost() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        harness.addToBattlefield(player1, new GlowcapLantern());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Glowcap Lantern");
        harness.assertNotOnBattlefield(player1, "Glowcap Lantern");
        assertThat(gd.stack).hasSize(1);
        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isFalse();

        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPermanent() {
        addCreatureReady(player1, new VitosInquisitor());
        harness.addToBattlefield(player2, new ArmoredKincaller());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vito's Inquisitor");
        harness.assertOnBattlefield(player2, "Armored Kincaller");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without black mana the ability cannot sacrifice a permanent")
    void requiresBlackMana() {
        addCreatureReady(player1, new VitosInquisitor());
        harness.addToBattlefield(player1, new GlowcapLantern());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Glowcap Lantern");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Inquisitor can activate repeatedly")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new VitosInquisitor());
        inquisitor.setSummoningSick(true);
        inquisitor.tap();
        harness.addToBattlefield(player1, new GlowcapLantern());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new ArmoredKincaller());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Glowcap Lantern");
        harness.assertInGraveyard(player1, "Armored Kincaller");
    }

    @Test
    @DisplayName("An ability whose source was sacrificed does not affect another Inquisitor")
    void removedSourceDoesNotGrantCountersOrMenaceToAnotherCopy() {
        Permanent first = addCreatureReady(player1, new VitosInquisitor());
        Permanent second = addCreatureReady(player1, new VitosInquisitor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.activateAbility(player1, 1, null, null);

        harness.assertInGraveyard(player1, "Vito's Inquisitor");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
