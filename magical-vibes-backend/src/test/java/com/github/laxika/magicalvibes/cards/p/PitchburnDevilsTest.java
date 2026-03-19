package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PitchburnDevilsTest extends BaseCardTest {

    /**
     * Sets up combat where Pitchburn Devils (player1) attacks and is blocked by a bigger creature (player2).
     * Devils (3/3) will die from combat damage against a 4/4.
     */
    private void setupCombatWhereDevilsDie() {
        Permanent devilsPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pitchburn Devils"))
                .findFirst().orElseThrow();
        devilsPerm.setSummoningSick(false);
        devilsPerm.setAttacking(true);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent blockerPerm = new Permanent(bigCreature);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Pitchburn Devils has ON_DEATH DealDamageToAnyTargetEffect(3)")
    void hasCorrectProperties() {
        PitchburnDevils card = new PitchburnDevils();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect dmg = (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(dmg.damage()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Pitchburn Devils puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PitchburnDevils()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pitchburn Devils"));
    }

    // ===== Death trigger — target creature =====

    @Test
    @DisplayName("When Pitchburn Devils dies in combat, controller is prompted to choose any target")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereDevilsDie();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pitchburn Devils"));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger deals 3 damage to chosen creature and destroys it if lethal")
    void deathTriggerDeals3DamageAndKillsCreature() {
        harness.addToBattlefield(player1, new PitchburnDevils());

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        harness.addToBattlefield(player2, bears);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereDevilsDie();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pitchburn Devils");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Death trigger — target player =====

    @Test
    @DisplayName("Death trigger deals 3 damage to chosen player")
    void deathTriggerDeals3DamageToPlayer() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.setLife(player2, 20);

        setupCombatWhereDevilsDie();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Death trigger can target own controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.setLife(player1, 20);

        setupCombatWhereDevilsDie();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Death trigger — board wipe =====

    @Test
    @DisplayName("Death trigger after Wrath of God still targets players")
    void deathTriggerAfterWrathTargetsPlayer() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pitchburn Devils"));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new PitchburnDevils());

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        harness.addToBattlefield(player2, bears);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereDevilsDie();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, bearsId);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
