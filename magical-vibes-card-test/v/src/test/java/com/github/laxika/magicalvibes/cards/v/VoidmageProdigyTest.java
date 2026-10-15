package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AphettoGrifter;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidmageProdigy.class, AphettoGrifter.class, ElvishWarrior.class, Shock.class})
class VoidmageProdigyTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlue() {
        Permanent prodigy = castFaceDown();
        assertThat(prodigy.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(prodigy));
        harness.passBothPriorities();

        assertThat(prodigy.isFaceDown()).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithoutBlueMana() {
        Permanent prodigy = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(prodigy)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(prodigy.isFaceDown()).isTrue();
    }

    @Test
    void sacrificesAWizardToCounterTargetSpell() {
        VoidmageProdigy prodigy = new VoidmageProdigy();
        harness.addToBattlefield(player1, prodigy);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voidmage Prodigy");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void countersCreatureSpell() {
        VoidmageProdigy prodigy = new VoidmageProdigy();
        harness.addToBattlefield(player1, prodigy);
        ElvishWarrior warrior = new ElvishWarrior();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, warrior, "{G}{G}");
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, warrior.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voidmage Prodigy");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    void onlyWizardsCanBeSacrificed() {
        VoidmageProdigy prodigy = new VoidmageProdigy();
        Permanent prodigyPermanent = harness.addToBattlefieldAndReturn(player1, prodigy);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new AphettoGrifter());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(wizard.getId(), prodigyPermanent.getId())
                .doesNotContain(warrior.getId());
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prodigyPermanent).doesNotContain(wizard);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void faceDownAndOpposingWizardsCannotPayTheSacrificeCost() {
        Permanent faceDown = castFaceDown();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VoidmageProdigy());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new AphettoGrifter());
        Permanent opposingWizard = harness.addToBattlefieldAndReturn(player2, new AphettoGrifter());
        wizard.tap();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 1, null, shock.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(source.getId(), wizard.getId());
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDown, source).doesNotContain(wizard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingWizard);
        harness.assertInGraveyard(player1, "Aphetto Grifter");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWithoutTwoBlueManaAndDoesNotSacrifice() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new VoidmageProdigy());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prodigy);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    void canTurnFaceUpInResponseAndImmediatelySacrificeItself() {
        Permanent prodigy = castFaceDown();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, player1.getId());
        harness.turnFaceUp(player1, 0);
        harness.activateAbility(player1, 0, null, shock.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(prodigy);
        harness.assertInGraveyard(player1, "Voidmage Prodigy");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new VoidmageProdigy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Voidmage Prodigy");
    }
}
