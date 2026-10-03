package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.d.Despondency;
import com.github.laxika.magicalvibes.cards.e.EssenceSliver;
import com.github.laxika.magicalvibes.cards.g.GoblinDynamo;
import com.github.laxika.magicalvibes.cards.i.ImperialHellkite;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.cards.s.SmokespewInvoker;
import com.github.laxika.magicalvibes.cards.s.SootfeatherFlock;
import com.github.laxika.magicalvibes.cards.u.UnstableHulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkromaAngelOfWrath.class, AvenEnvoy.class, DarkBanishing.class, Despondency.class,
        EssenceSliver.class, GoblinDynamo.class, ImperialHellkite.class, Pestilence.class,
        SmokespewInvoker.class, SootfeatherFlock.class, UnstableHulk.class})
class AkromaAngelOfWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from black prevents a black ability from targeting Akroma")
    void blackAbilityCannotTargetAkroma() {
        Permanent akroma = addCreatureReady(player2, new AkromaAngelOfWrath());
        addCreatureReady(player1, new SmokespewInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, akroma.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from red prevents a red ability from targeting Akroma")
    void redAbilityCannotTargetAkroma() {
        Permanent akroma = addCreatureReady(player2, new AkromaAngelOfWrath());
        addCreatureReady(player1, new GoblinDynamo());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, akroma.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Protection from black prevents a black spell from targeting Akroma")
    void blackSpellCannotTargetAkroma() {
        Permanent akroma = addCreatureReady(player2, new AkromaAngelOfWrath());
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("A black creature with flying cannot block Akroma")
    void blackCreatureCannotBlockAkroma() {
        addCreatureReady(player1, new AkromaAngelOfWrath());
        Permanent blocker = addCreatureReady(player2, new SootfeatherFlock());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A red creature with flying cannot block Akroma")
    void redCreatureCannotBlockAkroma() {
        addCreatureReady(player1, new AkromaAngelOfWrath());
        Permanent blocker = addCreatureReady(player2, new ImperialHellkite());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A blue creature with flying can block Akroma")
    void blueCreatureCanBlockAkroma() {
        addCreatureReady(player1, new AkromaAngelOfWrath());
        Permanent blocker = addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from a red creature")
    void redCombatDamageIsPrevented() {
        Permanent akroma = addCreatureReady(player2, new AkromaAngelOfWrath());
        Permanent hulk = castTurnedUpUnstableHulk();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(akroma);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hulk);
    }

    @Test
    @DisplayName("Protection from black prevents black damage")
    void blackDamageIsPrevented() {
        Permanent akroma = addCreatureReady(player2, new AkromaAngelOfWrath());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pestilence());
        harness.addMana(player1, ManaColor.BLACK, 6);

        for (int i = 0; i < 6; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(akroma);
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Haste allows Akroma to attack the turn it enters without tapping due to vigilance")
    void hasteAndVigilanceAllowImmediateUntappedAttack() {
        harness.castFromHand(player1, new AkromaAngelOfWrath(), "{5}{W}{W}{W}");
        harness.passBothPriorities();
        Permanent akroma = findPermanent(player1, "Akroma, Angel of Wrath");

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(akroma.isAttacking()).isTrue();
        assertThat(akroma.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Flying prevents a white ground creature from blocking Akroma")
    void groundCreatureCannotBlockAkroma() {
        addCreatureReady(player1, new AkromaAngelOfWrath());
        Permanent blocker = addCreatureReady(player2, new EssenceSliver());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Trample deals excess damage through a blue flying blocker")
    void trampleDealsOnlyExcessDamageToPlayer() {
        addCreatureReady(player1, new AkromaAngelOfWrath());
        addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Aven Envoy");
        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player1, "Akroma, Angel of Wrath");
    }

    @Test
    @DisplayName("First strike kills an attacking white Sliver before it can deal damage and gain life")
    void firstStrikeKillsAttackerBeforeItsDamageStep() {
        addCreatureReady(player1, new EssenceSliver());
        addCreatureReady(player2, new AkromaAngelOfWrath());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Essence Sliver");
        harness.assertOnBattlefield(player2, "Akroma, Angel of Wrath");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection from black prevents a black Aura from enchanting Akroma")
    void blackAuraCannotTargetAkroma() {
        Permanent akroma = addCreatureReady(player2, new AkromaAngelOfWrath());
        harness.setHand(player1, List.of(new Despondency()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private Permanent castTurnedUpUnstableHulk() {
        harness.setHand(player1, List.of(new UnstableHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hulk = findPermanent(player1, "Unstable Hulk");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hulk));
        harness.passBothPriorities();
        hulk.setSummoningSick(false);
        return hulk;
    }
}
