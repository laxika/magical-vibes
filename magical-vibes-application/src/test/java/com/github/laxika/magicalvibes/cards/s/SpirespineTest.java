package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeastOfDreams;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spirespine.class, GoldenHind.class, FontOfFertility.class, Hubris.class, FeastOfDreams.class})
class SpirespineTest extends BaseCardTest {

    @Test
    @DisplayName("Spirespine blocks each combat when cast as a creature")
    void creatureMustBlockEachCombat() {
        addCreatureReady(player2, new Spirespine());
        addCreatureReady(player1, new GoldenHind());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Bestow gives the enchanted creature +4/+1 and makes it block each combat")
    void bestowBoostsAndRequiresBlocking() {
        Permanent hind = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setHand(player1, List.of(new Spirespine()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, hind.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hind)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hind)).isEqualTo(2);

        addCreatureReady(player2, new GoldenHind());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Spirespine cannot target a noncreature permanent when bestowed")
    void cannotTargetNonCreature() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfFertility());
        harness.setHand(player1, List.of(new Spirespine()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, font.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A tapped Spirespine is not required to block")
    void tappedCreatureDoesNotHaveToBlock() {
        Permanent spirespine = addCreatureReady(player2, new Spirespine());
        spirespine.tap();
        Permanent attacker = addCreatureReady(player1, new GoldenHind());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Normal casting needs no target and the resulting creature must block")
    void normalCastCreatesCreatureWithBlockingRequirement() {
        harness.setHand(player1, List.of(new Spirespine()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        addCreatureReady(player2, new GoldenHind());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void bestowWithRemovedTargetBecomesCreatureAndMustBlock() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setHand(player1, List.of(new Spirespine()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castWithAlternateCost(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent spirespine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(spirespine.getCard()).isInstanceOf(Spirespine.class);
        assertThat(spirespine.isAttached()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        addCreatureReady(player2, new GoldenHind());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Spirespine stays on the battlefield and must block after its enchanted creature dies")
    void detachedBestowBecomesCreatureAndMustBlock() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setHand(player1, List.of(new Spirespine()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new FeastOfDreams()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent spirespine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(spirespine.getCard()).isInstanceOf(Spirespine.class);
        assertThat(spirespine.isAttached()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isInstanceOf(GoldenHind.class);

        addCreatureReady(player2, new GoldenHind());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
