package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ChandraAblaze;
import com.github.laxika.magicalvibes.cards.g.GoblinGuide;
import com.github.laxika.magicalvibes.cards.p.PunishingFire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableFooting.class, GoblinGuide.class, ChandraAblaze.class, PunishingFire.class})
class UnstableFootingTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it can be cast without a target and disables prevention this turn")
    void withoutKickerHasNoTargetAndDisablesPrevention() {
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("When kicked, it deals 5 damage to a target player despite prevention")
    void kickedDealsFiveDamageDespitePrevention() {
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A kicked spell must target a player or planeswalker")
    void kickedCannotTargetCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GoblinGuide());
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("The damage prevention lock wears off at end of turn")
    void preventionLockClearedAtEndOfTurn() {
        gd.damageCantBePreventedThisTurn = true;

        new com.github.laxika.magicalvibes.service.turn.TurnCleanupService(null, null)
                .resetEndOfTurnModifiers(gd);

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    @Test
    void kickedCanDamagePlaneswalker() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraAblaze());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castKickedInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Chandra Ablaze");
        harness.assertNotOnBattlefield(player2, "Chandra Ablaze");
        harness.assertLife(player2, 20);
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
    }

    @Test
    void kickedCanTargetItsController() {
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castKickedInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    void illegalKickedTargetStopsEntireSpellFromResolving() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraAblaze());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castKickedInstant(player1, 0, chandra.getId());
        gd.playerBattlefields.get(player2.getId()).remove(chandra);
        gd.playerGraveyards.get(player2.getId()).add(chandra.getCard());

        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
        harness.assertInGraveyard(player1, "Unstable Footing");
        harness.assertLife(player2, 20);
    }

    @Test
    void unkickedSpellDisablesPreventionForSubsequentDamageFromEitherPlayer() {
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new UnstableFooting(), new PunishingFire()));
        harness.setHand(player2, List.of(new PunishingFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    void preventionWorksAgainOnFollowingTurn() {
        harness.setHand(player1, List.of(new UnstableFooting()));
        harness.setHand(player2, List.of(new PunishingFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(8);
    }
}
