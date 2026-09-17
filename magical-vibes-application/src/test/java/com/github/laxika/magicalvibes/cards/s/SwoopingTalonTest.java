package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.f.FreneticRaptor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwoopingTalon.class, AvenEnvoy.class, FreneticRaptor.class})
class SwoopingTalonTest extends BaseCardTest {

    @Test
    @DisplayName("Provoke untaps the chosen creature and forces it to block")
    void provokeUntapsAndForcesBlock() {
        Permanent talon = addCreatureReady(player1, new SwoopingTalon());
        Permanent blocker = addCreatureReady(player2, new AvenEnvoy());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(blocker.getId());

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getMustBlockIds()).containsExactly(talon.getId());

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The activated ability removes flying until end of turn")
    void losesFlyingUntilEndOfTurn() {
        Permanent talon = addCreatureReady(player1, new SwoopingTalon());
        assertThat(gqs.hasKeyword(gd, talon, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, talon, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, talon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining provoke leaves the chosen creature unchanged")
    void decliningProvokeDoesNothing() {
        addCreatureReady(player1, new SwoopingTalon());
        Permanent blocker = addCreatureReady(player2, new AvenEnvoy());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Provoke only offers a defending player's creature")
    void provokeFiltersTargets() {
        addCreatureReady(player1, new SwoopingTalon());
        Permanent ownCreature = addCreatureReady(player1, new AvenEnvoy());
        Permanent defendingCreature = addCreatureReady(player2, new AvenEnvoy());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("Provoke has no target prompt when the defending player controls no creatures")
    void provokeWithoutLegalTargetDoesNotPrompt() {
        addCreatureReady(player1, new SwoopingTalon());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Provoke does not require a creature that cannot block to block")
    void cannotBlockTargetSatisfiesIfAbleRequirement() {
        Permanent talon = addCreatureReady(player1, new SwoopingTalon());
        Permanent blocker = addCreatureReady(player2, new FreneticRaptor());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        prepareDeclareBlockers();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
        assertThat(blocker.getMustBlockIds()).containsExactly(talon.getId());
    }

}
