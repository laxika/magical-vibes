package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExileTest extends BaseCardTest {

    private void castExile(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Exile()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    @Test
    @DisplayName("Exiles the nonwhite attacking creature and controller gains life equal to its toughness")
    void exilesAndGainsLife() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        castExile(attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears (2/2) exiled -> not on battlefield, not in graveyard, in exile
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
        // Caster gains life equal to toughness (2): 15 + 2 = 17
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Life gain accounts for toughness modifiers")
    void lifeGainAccountsForToughnessModifiers() {
        harness.setLife(player2, 10);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());
        attacker.setToughnessModifier(3); // 2 + 3 = 5 effective toughness

        castExile(attacker.getId());
        harness.passBothPriorities();

        // Effective toughness 5 -> caster gains 5 life (10 + 5 = 15)
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot target a white attacking creature")
    void cannotTargetWhiteAttackingCreature() {
        addAttacker(player2, new GrizzlyBears()); // valid nonwhite target elsewhere so spell is playable
        Permanent whiteAttacker = addAttacker(player1, new EliteVanguard());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Exile()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, whiteAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite attacking creature");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player2, new GrizzlyBears()); // valid target elsewhere so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Exile()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        castExile(attacker.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No life gain when the spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
