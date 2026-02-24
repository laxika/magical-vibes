package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContagionClaspTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB -1/-1 counter on target creature and proliferate activated ability")
    void hasCorrectEffectsAndAbility() {
        ContagionClasp card = new ContagionClasp();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).singleElement()
                .isInstanceOf(ProliferateEffect.class);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }

    // ===== ETB: put -1/-1 counter on target creature =====

    @Test
    @DisplayName("ETB puts a -1/-1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can kill a 1/1 creature")
    void etbKillsOneOneCreature() {
        // Create a 1/1 by giving a 2/2 Grizzly Bears a -1/-1 counter
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        UUID bearsId = bears.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Bears (1/1) got another -1/-1 counter making it 0/0, dies to SBA
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Proliferate activated ability =====

    @Test
    @DisplayName("Proliferate adds another -1/-1 counter to chosen creature")
    void proliferateAddsMinusCounters() {
        Permanent clasp = addReadyClasp(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // Now awaiting proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate adds another +1/+1 counter to chosen creature")
    void proliferateAddsPlusCounters() {
        Permanent clasp = addReadyClasp(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose none (empty selection)")
    void proliferateCanChooseNone() {
        Permanent clasp = addReadyClasp(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose nothing
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate adds counters to multiple permanents")
    void proliferateMultiplePermanents() {
        Permanent clasp = addReadyClasp(player1);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(bears1.getMinusOneMinusOneCounters()).isEqualTo(2);
        assertThat(bears2.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate kills creature with 0 toughness from additional -1/-1 counter")
    void proliferateKillsCreature() {
        Permanent clasp = addReadyClasp(player1);

        // Grizzly Bears (2/2) with 1 -1/-1 counter = 1/1, another makes it 0/0 → dies
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Bears should be dead (0/0 from SBA)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Proliferate does nothing when no permanents have counters")
    void proliferateNoEligiblePermanents() {
        Permanent clasp = addReadyClasp(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No MULTI_PERMANENT_CHOICE should be awaited — no eligible permanents
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-creature permanents without counters are not eligible for proliferate")
    void nonCreaturePermanentsWithoutCountersNotEligible() {
        Permanent clasp = addReadyClasp(player1);
        harness.addToBattlefield(player1, new Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No eligible permanents, no choice needed
        Permanent spellbook = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow();
        assertThat(spellbook.getMinusOneMinusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Proliferate requires tap (cannot activate when tapped)")
    void proliferateRequiresTap() {
        Permanent clasp = addReadyClasp(player1);
        clasp.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyClasp(Player player) {
        ContagionClasp card = new ContagionClasp();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
