package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarrentonMedic;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrentonMedic.class, Corrupt.class, JaceBeleren.class, Mountain.class, SafeholdSentry.class, Swamp.class})
class CorruptTest extends BaseCardTest {

    @Test
    @DisplayName("Corrupt targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Corrupt.class);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Corrupt targeting a creature puts it on the stack")
    void castingPutsOnStackUpstreamReview() {
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Corrupt deals damage to creature equal to Swamps controlled and gains life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // 3 damage kills Safehold Sentry (2 toughness)
        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
        // Controller gains 3 life (equal to Swamp count)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Corrupt deals damage to creature equal to Swamps controlled and gains life")
    void dealsDamageToCreatureAndGainsLifeUpstreamReview() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // 3 damage kills Safehold Sentry (2 toughness)
        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
        // Controller gains 3 life (equal to Swamp count)
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Corrupt deals damage to player equal to Swamps controlled and gains life")
    void dealsDamageToPlayerAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player 2 takes 4 damage
        harness.assertLife(player2, 16);
        // Player 1 gains 4 life
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Corrupt counts only controller's Swamps, not opponent's")
    void countsOnlyControllerSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // Only 1 damage (1 Swamp controlled by player1), Safehold Sentry survives (2 toughness)
        harness.assertOnBattlefield(player2, "Safehold Sentry");
        // Controller gains 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Corrupt counts only controller's Swamps, not opponent's")
    void countsOnlyControllerSwampsUpstreamReview() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // Only 1 damage (1 Swamp controlled by player1), Safehold Sentry survives (2 toughness)
        harness.assertOnBattlefield(player2, "Safehold Sentry");
        // Controller gains 1 life
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Corrupt counts Swamps at resolution time")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());

        // Remove all Swamps before resolution
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Swamp"));

        harness.passBothPriorities();

        // 0 Swamps at resolution, so 0 damage and 0 life gain
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Corrupt fizzles when target creature is removed before resolution — no life gain")
    void fizzlesWhenTargetCreatureRemoved() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castSorcery(player1, 0, targetId);

        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no damage to the opponent and no life gain despite controlling 2 Swamps
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Corrupt does not count non-Swamp lands")
    void doesNotCountNonSwampLands() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only 1 Swamp, so 1 damage and 1 life gained
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Corrupt gains life only for damage actually dealt")
    void gainsLifeOnlyForDamageActuallyDealt() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent medic = addCreatureReady(player2, new BarrentonMedic());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(medic), null,
                player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Corrupt gains life only for damage actually dealt when part of the damage is prevented")
    void preventedDamageDoesNotGrantLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Permanent medic = addCreatureReady(player2, new BarrentonMedic());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(medic), null,
                player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Corrupt targeting its controller restores life before checking for a loss")
    void selfDamageCanBeRecoveredBeforeStateBasedActions() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Corrupt");
    }

    @Test
    @DisplayName("Corrupt may target its controller's creature and grants its controller life")
    void damagesOwnCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, sentry.getId());

        harness.assertInGraveyard(player1, "Safehold Sentry");
        harness.assertNotOnBattlefield(player1, "Safehold Sentry");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Corrupt gains life for all damage dealt to a planeswalker, beyond its loyalty")
    void planeswalkerDamageIsNotCappedAtLoyalty() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, jace.getId());

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Corrupt counts newly acquired Swamps when it resolves")
    void countsSwampsAddedAfterCasting() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
