package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MishraTamerOfMakFawaTest extends BaseCardTest {

    @Test
    @DisplayName("Grants unearth to an artifact card in your graveyard")
    void grantsUnearthToOwnedArtifactCard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new IcyManipulator()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Icy Manipulator");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Does not grant unearth to a nonartifact card")
    void doesNotGrantUnearthToNonartifactCard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ward can be paid by sacrificing any permanent")
    void wardCanBePaidBySacrificingAnyPermanent() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spider.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Icy Manipulator");
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Artifact permanents you control have ward")
    void artifactPermanentsHaveWard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Icy Manipulator");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Disenchant");
    }
}
