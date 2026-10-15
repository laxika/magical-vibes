package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuraGnarlid;
import com.github.laxika.magicalvibes.cards.f.FrostwindInvoker;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfWizardry.class, AuraGnarlid.class, FrostwindInvoker.class, PropheticPrism.class})
class CurseOfWizardryTest extends BaseCardTest {

    private void castAndChooseColor(CardColor color) {
        harness.castFromHand(player1, new CurseOfWizardry(), "{2}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, color.name());
    }

    @Test
    @DisplayName("A player casting a spell of the chosen color loses 1 life")
    void playerCastingChosenColorSpellLosesLife() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new AuraGnarlid(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
    }

    @Test
    @DisplayName("The caster loses life even when an opponent controls Curse of Wizardry")
    void opponentCastingChosenColorSpellLosesLife() {
        castAndChooseColor(CardColor.GREEN);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.castFromHand(player2, new AuraGnarlid(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("A spell of another color does not cause life loss")
    void otherColorSpellDoesNotCauseLifeLoss() {
        castAndChooseColor(CardColor.RED);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new FrostwindInvoker(), "{4}{U}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void colorlessSpellDoesNotTriggerEvenWhenPaidWithChosenColorMana() {
        castAndChooseColor(CardColor.BLACK);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player1, "Prophetic Prism");
    }

    @Test
    void lifeLossResolvesBeforeTheChosenColorSpell() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new AuraGnarlid(), "{2}{G}");

        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 9);
        harness.assertNotOnBattlefield(player1, "Aura Gnarlid");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aura Gnarlid");
    }

    @Test
    void multipleCopiesEachCauseLifeLoss() {
        castAndChooseColor(CardColor.GREEN);
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new AuraGnarlid(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 8);
    }

    @Test
    void differentCopiesRememberTheirOwnChosenColors() {
        castAndChooseColor(CardColor.GREEN);
        castAndChooseColor(CardColor.BLUE);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new AuraGnarlid(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 9);

        harness.castFromHand(player1, new FrostwindInvoker(), "{4}{U}");
        harness.passBothPriorities();
        harness.assertLife(player1, 8);
    }

    @Test
    void doesNotTriggerForItsOwnCast() {
        harness.setLife(player1, 10);
        castAndChooseColor(CardColor.BLACK);

        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({MycosynthLattice.class})
    void spellMadeColorlessByMycosynthLatticeDoesNotTrigger() {
        castAndChooseColor(CardColor.GREEN);
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new AuraGnarlid(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @CardUsed({LeylineOfSanctity.class})
    void opponentHexproofDoesNotPreventNontargetedLifeLoss() {
        castAndChooseColor(CardColor.GREEN);
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.castFromHand(player2, new AuraGnarlid(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 9);
    }
}
