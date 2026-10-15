package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.o.OutpostSiege;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MangaraOfCorondor.class, AshcoatBear.class, Plains.class, MomentaryBlink.class, OutpostSiege.class})
class MangaraOfCorondorTest extends BaseCardTest {

    @BeforeEach
    void mainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Exiles Mangara and the targeted permanent")
    void exilesItselfAndTargetPermanent() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        assertThat(mangara.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId(), bear.getCard().getId());
    }

    @Test
    @DisplayName("Can target Mangara itself")
    void canTargetItself() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());

        harness.activateAbility(player1, 0, null, mangara.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId());
    }

    @Test
    @DisplayName("Stays on the battlefield when the target is illegal on resolution")
    void staysWhenTargetLeavesBeforeResolution() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mangara);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Can target a noncreature permanent")
    void exilesTargetLand() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(plains);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId(), plains.getCard().getId());
    }

    @Test
    @DisplayName("Still exiles the target when Mangara leaves before resolution")
    void exilesTargetIfSourceLeavesBeforeResolution() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mangara);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(bear.getCard().getId());
    }

    @Test
    @DisplayName("Only targets permanents")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new MangaraOfCorondor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mangara = harness.addToBattlefieldAndReturn(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mangara.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        mangara.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Blinking Mangara preserves the returned creature while the target is exiled")
    void blinkingSourceDoesNotExileReturnedMangara() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.castAndResolveInstant(player1, 0, mangara.getId());

        Permanent returnedMangara = findPermanent(player1, "Mangara of Corondor");
        assertThat(returnedMangara.getId()).isNotEqualTo(mangara.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returnedMangara);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(bear.getCard().getId())
                .doesNotContain(mangara.getCard().getId());
    }

    @Test
    @DisplayName("Blinking the target makes the ability fail without exiling Mangara")
    void blinkingTargetMakesOriginalTargetIllegal() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player2, List.of(new MomentaryBlink()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        Permanent returnedBear = findPermanent(player2, "Ashcoat Bear");
        assertThat(returnedBear.getId()).isNotEqualTo(bear.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mangara);
        assertThat(mangara.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returnedBear);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A targeted Outpost Siege sees Mangara leave simultaneously")
    void targetedSiegeTriggersForMangaraLeavingSimultaneously() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        harness.castFromHand(player1, new OutpostSiege(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dragons");
        Permanent siege = findPermanent(player1, "Outpost Siege");

        harness.activateAbility(player1, 0, null, siege.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara, siege);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId(), siege.getCard().getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
