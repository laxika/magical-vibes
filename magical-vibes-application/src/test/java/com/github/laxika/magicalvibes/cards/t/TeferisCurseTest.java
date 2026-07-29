package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferisCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Teferi's Curse can enchant an artifact")
    void canTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new TeferisCurse()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Teferi's Curse")
                        && artifact.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Teferi's Curse can't enchant a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.i.Island());
        Permanent land = findPermanent(player2, "Island");

        harness.setHand(player1, List.of(new TeferisCurse()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature phases out during its controller's untap step, taking the Curse with it")
    void enchantedCreaturePhasesOut() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent curse = attachCurse(bears);

        harness.forceActivePlayer(player1);
        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, curse);

        advanceTurn(); // player1's untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, curse);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears, curse);
        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
    }

    @Test
    @DisplayName("Enchanted permanent phases back in on its controller's next untap step")
    void phasesBackIn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent curse = attachCurse(bears);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn(); // phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);

        advanceTurn();
        advanceTurn(); // player1's next untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, curse);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isEmpty();
        assertThat(curse.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Permanent stops phasing once Teferi's Curse leaves the battlefield")
    void noPhasingAfterCurseRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent curse = attachCurse(bears);
        gd.playerBattlefields.get(player1.getId()).remove(curse);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    private Permanent attachCurse(Permanent host) {
        Permanent curse = new Permanent(new TeferisCurse());
        curse.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
        return curse;
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
