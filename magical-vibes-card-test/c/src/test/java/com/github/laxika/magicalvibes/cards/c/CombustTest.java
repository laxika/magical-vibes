package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Combust.class, WhiteKnight.class, AzureDrake.class, GoblinPiker.class, WallOfFrost.class, Cancel.class, SafePassage.class})
class CombustTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target white creature and kills it")
    void deals5DamageToWhiteCreature() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "White Knight");
        harness.assertInGraveyard(player2, "White Knight");
    }

    @Test
    @DisplayName("Deals 5 damage to target blue creature and kills it")
    void deals5DamageToBlueCreature() {
        harness.addToBattlefield(player2, new AzureDrake());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Azure Drake");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Azure Drake");
    }

    @Test
    @DisplayName("Cannot target non-white non-blue creature")
    void cannotTargetRedCreature() {
        harness.addToBattlefield(player2, new WhiteKnight()); // valid target so spell is playable
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");

        // Engine rejects invalid targets with an exception
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or blue creature");
    }

    @Test
    @DisplayName("Damage cannot be prevented by prevention shield")
    void damageCannotBePreventedByShield() {
        harness.addToBattlefield(player2, new AzureDrake());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Give the creature a damage prevention shield
        UUID targetId = harness.getPermanentId(player2, "Azure Drake");
        Permanent target = findPermanent(player2, "Azure Drake");
        target.setDamagePreventionShield(10);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Creature should still die despite prevention shield
        harness.assertNotOnBattlefield(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Azure Drake");
    }

    @Test
    @DisplayName("Large creature survives 5 unpreventable damage")
    void largeCreatureSurvives() {
        harness.addToBattlefield(player2, new WallOfFrost());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Wall of Frost");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Wall of Frost has 7 toughness and survives 5 damage
        harness.assertOnBattlefield(player2, "Wall of Frost");
        assertThat(findPermanent(player2, "Wall of Frost").getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Combust");
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Combust");
    }

    @Test
    @DisplayName("Cancel resolves without countering Combust")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new AzureDrake());
        Combust combust = new Combust();
        harness.setHand(player1, List.of(combust));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Azure Drake"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, combust.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Cancel");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Azure Drake");
        harness.assertInGraveyard(player1, "Combust");
    }

    @Test
    @DisplayName("Safe Passage cannot prevent Combust damage")
    void damageCannotBePreventedBySafePassage() {
        harness.addToBattlefield(player2, new WallOfFrost());
        harness.setHand(player1, List.of(new Combust()));
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Wall of Frost"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Frost");
        assertThat(findPermanent(player2, "Wall of Frost").getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Safe Passage");
    }

    @Test
    @DisplayName("Combust does not resolve if its target becomes only red")
    void doesNotResolveWhenTargetLosesWhiteAndBlue() {
        harness.addToBattlefield(player2, new WallOfFrost());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Wall of Frost");
        harness.castInstant(player1, 0, targetId);
        Permanent target = findPermanent(player2, "Wall of Frost");
        target.setColorOverridden(true);
        target.getTransientColors().clear();
        target.getTransientColors().add(CardColor.RED);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Frost");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Combust");
        assertThat(gd.stack).isEmpty();
    }
}
