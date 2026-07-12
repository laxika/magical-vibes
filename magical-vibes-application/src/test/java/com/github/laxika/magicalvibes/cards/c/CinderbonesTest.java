package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CinderbonesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts the ability on the stack targeting Cinderbones")
    void activatingRegenPutsOnStack() {
        Permanent perm = addCinderbonesReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCinderbonesReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent cinderbones = findPermanent(player1, "Cinderbones");
        assertThat(cinderbones.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Cinderbones from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCinderbonesReady(player1);
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cinderbones"));
        Permanent cinderbones = findPermanent(player1, "Cinderbones");
        assertThat(cinderbones.isTapped()).isTrue();
        assertThat(cinderbones.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cinderbones dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCinderbonesReady(player1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Cinderbones"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cinderbones"));
    }

    private Permanent addCinderbonesReady(Player player) {
        Permanent perm = new Permanent(new Cinderbones());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
