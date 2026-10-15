package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueenBrahne.class, Shock.class, GrizzlyBears.class})
class QueenBrahneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Queen Brahne creates a 0/1 black Wizard token")
    void attackCreatesWizardToken() {
        addQueen();

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.getCard().isToken()).isTrue();
        assertThat(wizard.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(wizard.getCard().getSubtypes()).containsExactly(CardSubtype.WIZARD);
        assertThat(gqs.getEffectivePower(gd, wizard)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wizard tokens deal 1 damage to each opponent for a noncreature spell")
    void wizardDamagesOpponentForNoncreatureSpell() {
        createWizard();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Wizard tokens do not trigger for a creature spell")
    void wizardDoesNotTriggerForCreatureSpell() {
        createWizard();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prowess gives Queen Brahne +1/+1 for a noncreature spell")
    void prowessBoostsQueen() {
        Permanent queen = addQueen();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's noncreature spells trigger neither prowess nor the Wizard")
    void opponentsSpellDoesNotTriggerAbilities() {
        createWizard();
        Permanent queen = findPermanent(player1, "Queen Brahne");
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wizard's cast trigger still deals damage after its source dies")
    void wizardTriggerResolvesAfterWizardDies() {
        Permanent wizard = createWizard();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wizard.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wizard")).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Prowess triggers once for each noncreature spell")
    void prowessAccumulatesAcrossSpells() {
        Permanent queen = addQueen();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature spells boost neither Queen Brahne nor the Wizard's damage")
    void creatureSpellTriggersNeitherAbility() {
        createWizard();
        Permanent queen = findPermanent(player1, "Queen Brahne");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wizard keeps its ability after Queen Brahne dies")
    void wizardAbilityIsIndependentOfQueen() {
        createWizard();
        Permanent queen = findPermanent(player1, "Queen Brahne");
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, queen.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Queen Brahne");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 18);
    }

    private Permanent createWizard() {
        addQueen();
        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Wizard");
    }

    private Permanent addQueen() {
        Permanent queen = addCreatureReady(player1, new QueenBrahne());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return queen;
    }
}
