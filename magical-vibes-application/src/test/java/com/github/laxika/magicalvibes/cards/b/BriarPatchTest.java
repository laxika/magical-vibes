package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BriarPatch.class, WildJhovall.class})
class BriarPatchTest extends BaseCardTest {

    private void addPatch() {
        harness.addToBattlefield(player1, new BriarPatch());
    }

    private Permanent addAttacker(Player controller) {
        return addCreatureReady(controller, new WildJhovall());
    }

    @Test
    @DisplayName("Creatures attacking its controller get -1/-0")
    void weakensCreaturesAttackingController() {
        addPatch();
        Permanent attacker = addAttacker(player2);
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures attacking another player are unaffected")
    void ignoresCreaturesAttackingSomeoneElse() {
        addPatch();
        Permanent attacker = addAttacker(player1);
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures that are not attacking are unaffected")
    void ignoresNonAttackingCreatures() {
        addPatch();
        Permanent creature = addAttacker(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff is not applied until the attack trigger resolves")
    void debuffWaitsForTriggerResolution() {
        addPatch();
        Permanent attacker = addAttacker(player2);

        declareAttackers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff remains until end of turn after the attacker leaves combat")
    void debuffRemainsAfterAttackerLeavesCombat() {
        addPatch();
        Permanent attacker = addAttacker(player2);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }
}
