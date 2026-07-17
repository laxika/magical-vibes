package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrokenVisageTest extends BaseCardTest {

    private void castBrokenVisage(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrokenVisage()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    private Permanent findSpiritToken(Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Destroys the attacking creature and creates a Spirit token with its power/toughness")
    void destroysAndCreatesSpirit() {
        Permanent attacker = addAttacker(player1);

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        // Grizzly Bears (2/2) destroyed -> owner's graveyard.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Caster gets a 2/2 Spirit token.
        Permanent spirit = findSpiritToken(player2);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spirit token's power/toughness reflect the destroyed creature's modified stats")
    void spiritCopiesModifiedStats() {
        Permanent attacker = addAttacker(player1);
        attacker.setPowerModifier(3);   // 2 + 3 = 5
        attacker.setToughnessModifier(1); // 2 + 1 = 3

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        Permanent spirit = findSpiritToken(player2);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Spirit token is sacrificed at the beginning of the next end step")
    void sacrificesSpiritAtEndStep() {
        Permanent attacker = addAttacker(player1);

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        // Token exists after resolution.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit"));

        // Advance to the end step — the token should be sacrificed.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player2); // legal target elsewhere so the spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrokenVisage()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
