package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.v.VisionSkeins;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulswornJury.class, MistralCharger.class, VisionSkeins.class})
class SoulswornJuryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell by paying mana and sacrificing itself")
    void countersCreatureSpell() {
        addCreatureReady(player1, new SoulswornJury());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        MistralCharger charger = new MistralCharger();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, charger, "{1}{W}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, charger.getId());

        harness.assertInGraveyard(player1, "Soulsworn Jury");
        harness.assertNotOnBattlefield(player1, "Soulsworn Jury");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mistral Charger");
        harness.assertNotOnBattlefield(player2, "Mistral Charger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetNonCreatureSpell() {
        addCreatureReady(player1, new SoulswornJury());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        VisionSkeins visionSkeins = new VisionSkeins();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, visionSkeins, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, visionSkeins.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soulsworn Jury");
        harness.assertNotInGraveyard(player1, "Soulsworn Jury");
    }

    @Test
    @DisplayName("Cannot activate without paying {1}{U}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new SoulswornJury());

        MistralCharger charger = new MistralCharger();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, charger, "{1}{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soulsworn Jury");
        harness.assertNotInGraveyard(player1, "Soulsworn Jury");
    }

    @Test
    @DisplayName("Defender prevents Soulsworn Jury from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new SoulswornJury());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        var jury = harness.addToBattlefieldAndReturn(player1, new SoulswornJury());
        jury.setSummoningSick(true);
        jury.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        MistralCharger charger = new MistralCharger();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, charger, "{1}{W}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, charger.getId());
        harness.assertInGraveyard(player1, "Soulsworn Jury");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mistral Charger");
        harness.assertNotOnBattlefield(player2, "Mistral Charger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's creature spell")
    void canCounterOwnCreatureSpell() {
        addCreatureReady(player1, new SoulswornJury());
        MistralCharger charger = new MistralCharger();
        harness.castFromHand(player1, charger, "{1}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, charger.getId());
        harness.assertInGraveyard(player1, "Soulsworn Jury");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mistral Charger");
        harness.assertNotOnBattlefield(player1, "Mistral Charger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the blue activation cost with two colorless mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new SoulswornJury());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        MistralCharger charger = new MistralCharger();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, charger, "{1}{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soulsworn Jury");
        harness.assertNotInGraveyard(player1, "Soulsworn Jury");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Mistral Charger");
        harness.assertNotInGraveyard(player2, "Mistral Charger");
        assertThat(gd.stack).isEmpty();
    }
}
