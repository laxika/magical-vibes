package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticConfluence.class, GrizzlyBears.class, Spellbook.class})
class MysticConfluenceTest extends BaseCardTest {

    @Test
    void repeatedDrawModeDrawsThreeCards() {
        harness.setHand(player1, List.of(new MysticConfluence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 2, 2, 2);
        harness.castAndResolveSorcery(player1, 0, modes);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void counterModeCountersTargetSpellAndDraws() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana(player2);

        harness.passPriority(player1);
        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 2, 2);
        harness.castInstant(player2, 0, modes, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void creatureModeReturnsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticConfluence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2);
        harness.castSorcery(player1, 0, modes, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void creatureModeRejectsNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new MysticConfluence()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, modes, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedCreatureModeReturnsThreeDifferentTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticConfluence()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 1);
        harness.castModalInstant(player1, 0, modes, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void repeatedCreatureModeCanTargetSameCreatureAndStillDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticConfluence()));
        harness.setLibrary(player1, List.of(new Spellbook()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 2);
        harness.castModalInstant(player1, 0, modes, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player1, "Spellbook");
    }

    @Test
    void repeatedCounterModeRequiresSeparatePaymentsAndResumesDraw() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new Spellbook()));
        addMana(player2);
        harness.passPriority(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 2);
        harness.castModalInstant(player2, 0, modes, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertNotInHand(player2, "Spellbook");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player2, "Spellbook");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void payingForOnlyOneRepeatedCounterModeDoesNotSaveSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new Spellbook()));
        addMana(player2);
        harness.passPriority(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 2);
        harness.castModalInstant(player2, 0, modes, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void allThreeModesCounterBounceAndDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears spell = new GrizzlyBears();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new Spellbook()));
        addMana(player2);
        harness.passPriority(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 1, 2);
        harness.castModalInstant(player2, 0, modes, List.of(spell.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    void oneRemainingLegalTargetAllowsBounceAndDraw() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticConfluence()));
        harness.setLibrary(player1, List.of(new Spellbook()));
        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));
        addMana(player1);
        addMana(player2);

        int originalModes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 2);
        harness.castModalInstant(player1, 0, originalModes, List.of(first.getId(), second.getId()));
        harness.passPriority(player1);
        int responseModes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2);
        harness.castModalInstant(player2, 0, responseModes, List.of(first.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    void losingOnlyTargetBeforeResolutionPreventsDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticConfluence()));
        harness.setLibrary(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));
        addMana(player1);
        addMana(player2);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2);
        harness.castModalInstant(player1, 0, modes, List.of(creature.getId()));
        harness.passPriority(player1);
        harness.castModalInstant(player2, 0, modes, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Mystic Confluence");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 5);
    }
}
