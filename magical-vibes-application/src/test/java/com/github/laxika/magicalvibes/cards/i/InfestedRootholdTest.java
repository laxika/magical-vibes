package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.l.LeoninBola;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfestedRoothold.class, DarksteelIngot.class, CrazedGoblin.class, LeoninBola.class})
class InfestedRootholdTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting an artifact spell may create a green Insect token")
    void opponentArtifactSpellCreatesInsect() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        prepareOpponentArtifactSpell();

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
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("An opponent casting a nonartifact spell does not trigger")
    void opponentNonartifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        prepareOpponentNonartifactSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("A controller casting an artifact spell does not trigger")
    void controllerArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new InfestedRoothold());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new DarksteelIngot(), "{3}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("Protection from artifacts prevents an artifact ability from targeting it")
    void protectionFromArtifactsPreventsArtifactAbilityTargeting() {
        Permanent roothold = addCreatureReady(player1, new InfestedRoothold());
        addCreatureReady(player1, new LeoninBola());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, roothold.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Defender prevents Infested Roothold from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new InfestedRoothold());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareOpponentArtifactSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DarksteelIngot(), "{3}");
    }

    private void prepareOpponentNonartifactSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new CrazedGoblin(), "{R}");
    }
}
