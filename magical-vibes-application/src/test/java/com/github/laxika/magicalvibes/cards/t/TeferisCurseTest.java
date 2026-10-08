package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisCurse.class, BayFalcon.class, CharcoalDiamond.class, Forest.class})
class TeferisCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Teferi's Curse can enchant an artifact")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CharcoalDiamond());

        harness.setHand(player1, List.of(new TeferisCurse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Teferi's Curse")
                        && artifact.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Teferi's Curse can't enchant a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new TeferisCurse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature phases out during its controller's untap step, taking the Curse with it")
    void enchantedCreaturePhasesOut() {
        Permanent creature = addCreatureReady(player1, new BayFalcon());
        Permanent curse = attachCurse(creature);

        harness.forceActivePlayer(player1);
        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, curse);

        advanceTurn(); // player1's untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, curse);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, curse);
        assertThat(gqs.findPermanentById(gd, creature.getId())).isNull();
    }

    @Test
    @DisplayName("Enchanted artifact phases out during its controller's untap step, taking the Curse with it")
    void enchantedArtifactPhasesOut() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CharcoalDiamond());
        Permanent curse = attachCurse(artifact);

        harness.forceActivePlayer(player1);
        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact, curse);

        advanceTurn(); // player1's untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact, curse);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(artifact, curse);
        assertThat(gqs.findPermanentById(gd, artifact.getId())).isNull();
    }

    @Test
    @DisplayName("Enchanted permanent phases back in on its controller's next untap step")
    void phasesBackIn() {
        Permanent creature = addCreatureReady(player1, new BayFalcon());
        Permanent curse = attachCurse(creature);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn(); // phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);

        advanceTurn();
        advanceTurn(); // player1's next untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, curse);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isEmpty();
        assertThat(curse.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Permanent stops phasing once Teferi's Curse leaves the battlefield")
    void noPhasingAfterCurseRemoved() {
        Permanent creature = addCreatureReady(player1, new BayFalcon());
        Permanent curse = attachCurse(creature);
        gd.playerBattlefields.get(player1.getId()).remove(curse);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("An opposing creature and the Curse phase together on the creature controller's untap steps")
    void opposingCreatureAndCursePhaseTogether() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        harness.setHand(player1, List.of(new TeferisCurse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent curse = findPermanent(player1, "Teferi's Curse");

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(curse);

        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(curse);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(curse);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(curse);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(curse);

        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(curse);
        assertThat(curse.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Teferi's Curse");
    }

    @Test
    @DisplayName("A tapped enchanted artifact phases out before untapping and untaps when it phases in")
    void tappedArtifactUntapsOnlyWhenItPhasesIn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CharcoalDiamond());
        artifact.tap();
        Permanent curse = attachCurse(artifact);

        harness.performUntapStep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(artifact, curse);
        assertThat(artifact.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(artifact, curse);
        assertThat(artifact.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact, curse);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(curse.getAttachedTo()).isEqualTo(artifact.getId());
    }

    private Permanent attachCurse(Permanent host) {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new TeferisCurse());
        curse.setAttachedTo(host.getId());
        return curse;
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.UPKEEP);
    }
}
