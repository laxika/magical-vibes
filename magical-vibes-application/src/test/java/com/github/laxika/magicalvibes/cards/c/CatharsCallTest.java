package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatharsCallTest extends BaseCardTest {

    private Permanent attach(Player auraController, Permanent host) {
        Permanent aura = new Permanent(new CatharsCall());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private Permanent addCreature(Player owner) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void runEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void grantsVigilance() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("At the beginning of the enchanted controller's end step, creates a 1/1 white Human token")
    void createsHumanTokenOnEndStep() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        int before = gd.playerBattlefields.get(player1.getId()).size();
        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before + 1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
    }

    @Test
    @DisplayName("Token is created for the enchanted permanent's controller, not the Aura's")
    void tokenGoesToEnchantedController() {
        Permanent host = addCreature(player2);
        attach(player1, host);

        runEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream().filter(p -> p.getCard().isToken()))
                .isEmpty();

        int before = gd.playerBattlefields.get(player2.getId()).size();
        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(before + 1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream().anyMatch(p -> p.getCard().isToken()))
                .isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().anyMatch(p -> p.getCard().isToken()))
                .isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CatharsCall()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
