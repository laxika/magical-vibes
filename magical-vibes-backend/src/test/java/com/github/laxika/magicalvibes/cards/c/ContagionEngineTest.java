package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContagionEngineTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB -1/-1 counter on each creature target player controls and proliferate twice activated ability")
    void hasCorrectEffectsAndAbility() {
        ContagionEngine card = new ContagionEngine();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .allSatisfy(e -> assertThat(e).isInstanceOf(ProliferateEffect.class));
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }

    // ===== ETB: put -1/-1 counter on each creature target player controls =====

    @Test
    @DisplayName("ETB puts -1/-1 counter on each creature target opponent controls")
    void etbPutsCountersOnAllOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> opponentBattlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : opponentBattlefield) {
            if (p.getCard().getName().equals("Grizzly Bears")) {
                assertThat(p.getMinusOneMinusOneCounters()).isEqualTo(1);
                assertThat(p.getEffectivePower()).isEqualTo(1);
                assertThat(p.getEffectiveToughness()).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("ETB does not affect controller's creatures")
    void etbDoesNotAffectControllerCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Controller's bears should be unaffected
        Permanent ownBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(ownBears.getMinusOneMinusOneCounters()).isEqualTo(0);

        // Opponent's bears should have a counter
        Permanent oppBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(oppBears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not affect non-creature permanents")
    void etbDoesNotAffectNonCreatures() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent spellbook = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow();
        assertThat(spellbook.getMinusOneMinusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB kills 1/1 creatures with -1/-1 counter")
    void etbKillsOneOneCreatures() {
        Permanent weakBears = new Permanent(new GrizzlyBears());
        weakBears.setMinusOneMinusOneCounters(1); // 2/2 with one -1/-1 = 1/1
        gd.playerBattlefields.get(player2.getId()).add(weakBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Bears (1/1) got another -1/-1 counter making it 0/0, dies to SBA
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Proliferate twice activated ability =====

    @Test
    @DisplayName("Proliferate twice adds two -1/-1 counters to chosen creature")
    void proliferateTwiceAddsDoubleCounters() {
        Permanent engine = addReadyEngine(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // First proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Second proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Proliferate twice can choose different permanents each time")
    void proliferateTwiceCanChooseDifferentTargets() {
        Permanent engine = addReadyEngine(player1);

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

        // First proliferate: choose bears1 only
        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId()));

        // Second proliferate: choose bears2 only
        harness.handleMultiplePermanentsChosen(player1, List.of(bears2.getId()));

        assertThat(bears1.getMinusOneMinusOneCounters()).isEqualTo(2);
        assertThat(bears2.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate twice can choose none for both")
    void proliferateTwiceCanChooseNone() {
        Permanent engine = addReadyEngine(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose nothing for both proliferates
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate twice requires tap (cannot activate when tapped)")
    void proliferateTwiceRequiresTap() {
        Permanent engine = addReadyEngine(player1);
        engine.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Proliferate twice kills creature when counters bring toughness to zero")
    void proliferateTwiceKillsCreature() {
        Permanent engine = addReadyEngine(player1);

        // Grizzly Bears (2/2) with 1 -1/-1 counter = 1/1
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // First proliferate: add counter (now 2 -1/-1 counters = effective 0/0,
        // but SBA are not checked during ability resolution per MTG Rule 704.3)
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Second proliferate: bears is still on battlefield (SBA deferred),
        // choose nothing so it stays at 2 counters
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // After ability fully resolves, SBA kills bears (2/2 with 2 -1/-1 = 0/0)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadyEngine(Player player) {
        ContagionEngine card = new ContagionEngine();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
