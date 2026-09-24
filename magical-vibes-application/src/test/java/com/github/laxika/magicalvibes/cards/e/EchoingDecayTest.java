package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoingDecay.class, DarksteelGargoyle.class, CrazedGoblin.class, DarksteelIngot.class})
class EchoingDecayTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target and all creatures with the same name -2/-2")
    void debuffsTargetAndAllSameNameCreatures() {
        Permanent ownGargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent otherGargoyle = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent goblin = addCreatureReady(player2, new CrazedGoblin());

        castEchoingDecay(target.getId());

        assertThat(gqs.getEffectivePower(gd, ownGargoyle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGargoyle)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherGargoyle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherGargoyle)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("A same-name hexproof creature is affected without being targeted")
    void affectsSameNameHexproofCreature() {
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent hexproof = addCreatureReady(player2, new DarksteelGargoyle());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        castEchoingDecay(target.getId());

        assertThat(gqs.getEffectiveToughness(gd, hexproof)).isEqualTo(1);
    }

    @Test
    @DisplayName("The reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());

        castEchoingDecay(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal reduction puts every same-name creature into its owner's graveyard")
    void killsAllSameNameCreatures() {
        addCreatureReady(player1, new CrazedGoblin());
        Permanent target = addCreatureReady(player2, new CrazedGoblin());
        addCreatureReady(player2, new CrazedGoblin());
        addCreatureReady(player2, new DarksteelGargoyle());

        castEchoingDecay(target.getId());

        harness.assertNotOnBattlefield(player1, "Crazed Goblin");
        harness.assertNotOnBattlefield(player2, "Crazed Goblin");
        harness.assertInGraveyard(player1, "Crazed Goblin");
        harness.assertInGraveyard(player2, "Crazed Goblin");
        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
    }

    @Test
    @DisplayName("Does not target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ingot.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses current creature names when finding affected creatures")
    @CardUsed(WitnessProtection.class)
    void usesEffectiveNamesWhenFindingSameNameCreatures() {
        Permanent target = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent renamed = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WitnessProtection());
        aura.setAttachedTo(renamed.getId());

        assertThat(gqs.getEffectiveName(gd, renamed)).isEqualTo("Legitimate Businessperson");

        castEchoingDecay(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(renamed);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, renamed)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, renamed)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent otherGargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, otherGargoyle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherGargoyle)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEchoingDecay(UUID targetId) {
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
