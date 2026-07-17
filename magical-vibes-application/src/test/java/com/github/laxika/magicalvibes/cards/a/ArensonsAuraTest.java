package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArensonsAuraTest extends BaseCardTest {

    // ===== {W}, Sacrifice an enchantment: Destroy target enchantment =====

    @Test
    @DisplayName("Destroys target enchantment, sacrificing itself to pay the cost")
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Only enchantment player1 controls is Arenson's Aura → auto-sacrificed
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
        // Aura sacrificed to pay the cost
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Arenson's Aura"));
    }

    @Test
    @DisplayName("Destroy ability cannot target a creature")
    void destroyCannotTargetCreature() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== {3}{U}{U}: Counter target enchantment spell =====

    @Test
    @DisplayName("Counters a target enchantment spell")
    void countersEnchantmentSpell() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addMana(player1, ManaColor.BLUE, 5);

        AngelicChorus chorus = new AngelicChorus();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(chorus));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castEnchantment(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, chorus.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Counter ability cannot target a creature spell")
    void counterCannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addMana(player1, ManaColor.BLUE, 5);

        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
