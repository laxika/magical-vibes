package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfestedRootholdTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting an artifact spell may create a green Insect token")
    void opponentArtifactSpellCreatesInsect() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        prepareOpponentArtifactSpell();

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Insect");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger creates no Insect token")
    void decliningCreatesNoInsect() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        prepareOpponentArtifactSpell();

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("An opponent casting a nonartifact spell does not trigger")
    void opponentNonartifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        prepareOpponentNonartifactSpell();

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("A controller casting an artifact spell does not trigger")
    void controllerArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        harness.setHand(player1, List.of(new EtheriumSculptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    private void prepareOpponentArtifactSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EtheriumSculptor()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    private void prepareOpponentNonartifactSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
    }
}
