package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.a.AuriokTransfixer;
import com.github.laxika.magicalvibes.cards.s.SphereOfPurity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConfusionInTheRanks.class, Atog.class, AuriokTransfixer.class,
        SphereOfPurity.class, AetherSpellbomb.class, ChromaticSphere.class})
class ConfusionInTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("The entering permanent's controller chooses a matching permanent and exchanges control")
    void enteringControllerChoosesCreatureTarget() {
        harness.addToBattlefield(player1, new ConfusionInTheRanks());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AuriokTransfixer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Atog()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Atog");
        harness.assertOnBattlefield(player2, "Auriok Transfixer");
    }

    @Test
    @DisplayName("A nonmatching permanent is not a legal target")
    void nonmatchingPermanentCannotBeChosen() {
        harness.addToBattlefield(player1, new ConfusionInTheRanks());
        harness.addToBattlefieldAndReturn(player1, new SphereOfPurity());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Atog()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sphere of Purity");
        harness.assertOnBattlefield(player2, "Atog");
    }

    @Test
    @DisplayName("An artifact entry can exchange control of a matching artifact")
    void artifactEntryTriggers() {
        harness.addToBattlefield(player1, new ConfusionInTheRanks());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChromaticSphere());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AetherSpellbomb()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aether Spellbomb");
        harness.assertOnBattlefield(player2, "Chromatic Sphere");
    }

    @Test
    @DisplayName("Confusion in the Ranks triggers on its own entry and can exchange enchantments")
    void enchantmentEntryTriggers() {
        harness.addToBattlefield(player2, new SphereOfPurity());

        harness.setHand(player1, List.of(new ConfusionInTheRanks()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Sphere of Purity");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sphere of Purity");
        harness.assertOnBattlefield(player2, "Confusion in the Ranks");
    }
}
