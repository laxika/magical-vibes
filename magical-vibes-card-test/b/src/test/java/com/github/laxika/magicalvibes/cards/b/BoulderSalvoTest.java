package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CliffhavenVampire;
import com.github.laxika.magicalvibes.cards.k.KozileksPathfinder;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.s.SweepAway;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoulderSalvo.class, BruteStrength.class, CliffhavenVampire.class,
        KozileksPathfinder.class, Negate.class, SweepAway.class, Wastes.class})
class BoulderSalvoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Kozilek's Pathfinder");
    }

    @Test
    @DisplayName("Casts for its surge cost after another spell was cast")
    void castsForSurgeCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new BruteStrength(), new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Boulder Salvo");
    }

    @Test
    @DisplayName("Cannot cast for its surge cost before another spell was cast")
    void surgeCostRequiresAnotherSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wastes());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Four damage kills a creature with exactly four toughness, including your own")
    void killsOwnCreatureWithFourToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CliffhavenVampire());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Cliffhaven Vampire");
        harness.assertInGraveyard(player1, "Cliffhaven Vampire");
        harness.assertInGraveyard(player1, "Boulder Salvo");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay only the surge mana without choosing an available surge cost")
    void normalCostStillRequiresFiveMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boulder Salvo");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent's spell does not enable surge")
    void opponentsSpellDoesNotEnableSurge() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player2, List.of(new BruteStrength()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boulder Salvo");
    }

    @Test
    @DisplayName("Playing a land does not count as casting a spell for surge")
    void landDoesNotEnableSurge() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new Wastes(), new BoulderSalvo()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boulder Salvo");
    }

    @Test
    @DisplayName("A countered spell still enables surge")
    void counteredSpellEnablesSurge() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        BruteStrength firstSpell = new BruteStrength();
        harness.setHand(player1, List.of(firstSpell, new BoulderSalvo()));
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, firstSpell.getId());
        harness.assertInGraveyard(player1, "Brute Strength");
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Boulder Salvo");
    }

    @Test
    @DisplayName("Surge does not allow casting a sorcery while another spell is on the stack")
    void surgeDoesNotGrantInstantTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new BruteStrength(), new BoulderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boulder Salvo");
    }

    @Test
    @DisplayName("A spell cast on the previous turn does not enable surge")
    void previousTurnsSpellDoesNotEnableSurge() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setLibrary(player2, List.of(new Wastes()));
        harness.setHand(player2, List.of(new BruteStrength()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BoulderSalvo()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals no damage when its only target leaves before resolution")
    void targetLeavingBattlefieldPreventsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new BoulderSalvo()));
        harness.setHand(player2, List.of(new SweepAway()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kozilek's Pathfinder");
        harness.assertInGraveyard(player1, "Boulder Salvo");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
