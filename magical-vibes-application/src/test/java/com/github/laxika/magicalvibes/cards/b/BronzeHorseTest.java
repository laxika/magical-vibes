package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
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

@CardUsed({BronzeHorse.class, ChainLightning.class, DAvenantArcher.class, KoboldsOfKherKeep.class})
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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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

    private void castChainLightningAt(Permanent target) {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }
}
