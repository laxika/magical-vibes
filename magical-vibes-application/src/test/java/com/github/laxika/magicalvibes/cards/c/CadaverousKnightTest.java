package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CadaverousKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Cadaverous Knight");
        assertThat(knight.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Cadaverous Knight from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addKnightReady(player1);
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cadaverous Knight");
        Permanent knight = findPermanent(player1, "Cadaverous Knight");
        assertThat(knight.isTapped()).isTrue();
        assertThat(knight.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cadaverous Knight dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addKnightReady(player1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cadaverous Knight");
        harness.assertInGraveyard(player1, "Cadaverous Knight");
    }

    private Permanent addKnightReady(Player player) {
        Permanent perm = new Permanent(new CadaverousKnight());
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
