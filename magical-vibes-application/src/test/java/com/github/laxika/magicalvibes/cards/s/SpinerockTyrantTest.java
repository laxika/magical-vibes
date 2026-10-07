package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinerockTyrant.class, AirElemental.class, Shock.class, DualShot.class, LavaAxe.class})
class SpinerockTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("A single-target sorcery is copied without paying its mana cost again")
    void copiesSingleTargetSorcery() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell cast with no targets does not trigger")
    void doesNotTriggerForZeroTargets() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Keeping the original target gives both damage spells wither")
    void keepsOriginalTarget() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinerockTyrant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Wither does not change damage dealt to a player")
    void copiedSpellStillDealsPlayerDamage() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An opponent's single-target spell does not trigger the Tyrant")
    void doesNotCopyOpponentsSpell() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("May copy a single-target instant and both spells gain wither")
    void copiesSingleTargetSpellWithWither() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, firstTarget.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstTarget.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(firstTarget.getMarkedDamage()).isZero();
        assertThat(secondTarget.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(secondTarget.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the copy leaves the original spell without wither")
    void decliningCopyDoesNotGrantWither() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell with more than one target does not trigger")
    void doesNotTriggerForMultipleTargets() {
        harness.addToBattlefield(player1, new SpinerockTyrant());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
