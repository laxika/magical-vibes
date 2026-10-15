package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.s.ScrollOfTheMasters;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchfiendOfDepravity.class, FeralKrushok.class, ScrollOfTheMasters.class})
class ArchfiendOfDepravityTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent keeps up to two creatures and sacrifices the rest")
    void opponentKeepsUpToTwoCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new ScrollOfTheMasters());

        advanceToEndStep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(first, second, noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(third.getCard());
    }

    @Test
    @DisplayName("The opponent may keep one or no creatures")
    void opponentMayKeepFewerThanTwoCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());

        advanceToEndStep(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(second.getCard(), third.getCard());
    }

    @Test
    @DisplayName("The opponent's choice may spare no creatures")
    void opponentMayKeepNoCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());

        advanceToEndStep(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("It does not trigger on its controller's end step")
    void doesNotTriggerOnControllersEndStep() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("An opponent with one creature may keep it")
    void opponentMayKeepTheirOnlyCreature() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());

        advanceToEndStep(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent without creatures makes no choice and keeps noncreatures")
    void opponentWithoutCreaturesMakesNoChoice() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfDepravity());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrollOfTheMasters());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(archfiend);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent's end step does not sacrifice the controller's creatures")
    void controllersCreaturesAreUnaffected() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfDepravity());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());
        harness.addToBattlefield(player2, new FeralKrushok());

        advanceToEndStep(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(archfiend, first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
