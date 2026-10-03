package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzeHorse.class, ChainLightning.class, DAvenantArcher.class, KoboldsOfKherKeep.class,
        Pyrotechnics.class})
class BronzeHorseTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from a spell that targets it while you control another creature")
    void preventsTargetingSpellDamageWithAnotherCreature() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        addCreatureReady(player2, new KoboldsOfKherKeep());

        castChainLightningAt(horse);

        assertThat(horse.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Bronze Horse");
    }

    @Test
    @DisplayName("Does not prevent targeted spell damage without another creature")
    void doesNotPreventTargetingSpellDamageWithoutAnotherCreature() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());

        castChainLightningAt(horse);

        assertThat(horse.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count a creature controlled by the opponent")
    void doesNotCountCreatureControlledByOpponent() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        addCreatureReady(player1, new KoboldsOfKherKeep());

        castChainLightningAt(horse);

        assertThat(horse.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not prevent damage from an ability")
    void doesNotPreventAbilityDamage() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        horse.setAttacking(true);
        addCreatureReady(player1, new DAvenantArcher());

        harness.activateAbility(player1, 0, null, horse.getId());
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent horse = addCreatureReady(player1, new BronzeHorse());
        Permanent blocker = addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3
        ));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(horse);
    }

    @Test
    @DisplayName("Prevents only its own damage from a spell with multiple targets")
    void preventsOnlyItsOwnDividedSpellDamage() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, Map.of(horse.getId(), 3, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed({Earthquake.class})
    @DisplayName("Does not prevent damage from a spell that does not target it")
    void doesNotPreventUntargetedSpellDamage() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        Permanent archer = addCreatureReady(player2, new DAvenantArcher());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(horse.getMarkedDamage()).isEqualTo(1);
        assertThat(archer.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "D'Avenant Archer");
    }

    @Test
    @DisplayName("Another Bronze Horse satisfies the condition for both Horses")
    void twoHorsesProtectEachOther() {
        Permanent first = addCreatureReady(player2, new BronzeHorse());
        Permanent second = addCreatureReady(player2, new BronzeHorse());
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 2));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Checks for another creature when damage is dealt, rather than when the spell is cast")
    void losesPreventionWhenOtherCreatureDiesInResponse() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        Permanent kobold = addCreatureReady(player2, new KoboldsOfKherKeep());
        kobold.setAttacking(true);
        addCreatureReady(player1, new DAvenantArcher());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, horse.getId());
        harness.activateAbility(player1, 0, null, kobold.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Kobolds of Kher Keep");
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not prevent combat damage while another creature is controlled")
    void doesNotPreventCombatDamage() {
        Permanent horse = addCreatureReady(player1, new BronzeHorse());
        addCreatureReady(player1, new KoboldsOfKherKeep());
        addCreatureReady(player2, new DAvenantArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                harness.getPermanentId(player2, "D'Avenant Archer"), 2,
                player2.getId(), 2
        ));

        assertThat(horse.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "D'Avenant Archer");
        harness.assertLife(player2, 18);
    }

    private void castChainLightningAt(Permanent target) {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }
}
