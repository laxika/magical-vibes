package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.Wingcrafter;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FloweringLumberknot.class, Wingcrafter.class, GrizzlyBears.class, TurnToFrog.class})
class FloweringLumberknotTest extends BaseCardTest {

    private void pair(Permanent a, Permanent b) {
        a.setPairedWithId(b.getId());
        b.setPairedWithId(a.getId());
    }

    @Test
    @DisplayName("Unpaired Flowering Lumberknot can't attack")
    void unpairedCantAttack() {
        addCreatureReady(player1, new FloweringLumberknot());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paired Flowering Lumberknot can attack")
    void pairedCanAttack() {
        Permanent lumberknot = addCreatureReady(player1, new FloweringLumberknot());
        Permanent wingcrafter = addCreatureReady(player1, new Wingcrafter());
        pair(lumberknot, wingcrafter);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Unpaired Flowering Lumberknot can't block")
    void unpairedCantBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new FloweringLumberknot());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paired Flowering Lumberknot can block")
    void pairedCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent lumberknot = addCreatureReady(player2, new FloweringLumberknot());
        Permanent wingcrafter = addCreatureReady(player2, new Wingcrafter());
        pair(lumberknot, wingcrafter);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(lumberknot.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lumberknot can't attack when its paired partner loses soulbond")
    void partnerLosingSoulbondPreventsAttacking() {
        Permanent lumberknot = addCreatureReady(player1, new FloweringLumberknot());
        Permanent wingcrafter = addCreatureReady(player1, new Wingcrafter());
        pair(lumberknot, wingcrafter);

        turnToFrog(wingcrafter);

        assertThat(lumberknot.getPairedWithId()).isEqualTo(wingcrafter.getId());
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.SOULBOND)).isFalse();
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lumberknot can't block when its paired partner loses soulbond")
    void partnerLosingSoulbondPreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lumberknot = addCreatureReady(player2, new FloweringLumberknot());
        Permanent wingcrafter = addCreatureReady(player2, new Wingcrafter());
        pair(lumberknot, wingcrafter);

        turnToFrog(wingcrafter);

        assertThat(lumberknot.getPairedWithId()).isEqualTo(wingcrafter.getId());
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.SOULBOND)).isFalse();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unpaired Lumberknot can attack after losing its own abilities")
    void losingOwnAbilitiesRemovesAttackRestriction() {
        Permanent lumberknot = addCreatureReady(player1, new FloweringLumberknot());

        turnToFrog(lumberknot);
        declareAttackers(List.of(0));

        harness.assertLife(player2, 19);
    }

    private void turnToFrog(Permanent target) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
