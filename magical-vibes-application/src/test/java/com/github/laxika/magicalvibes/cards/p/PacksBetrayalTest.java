package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PacksBetrayal.class, Forest.class, GrizzlyBears.class, GreaterWerewolf.class, WyluliWolf.class})
class PacksBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, and grants haste, then scries with a Wolf")
    void wolfEnablesScry() {
        Permanent target = addTappedCreature(player2);
        harness.addToBattlefield(player1, new WyluliWolf());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castPackBetrayal(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Werewolf also enables scry 2")
    void werewolfEnablesScry() {
        Permanent target = addTappedCreature(player2);
        harness.addToBattlefield(player1, new GreaterWerewolf());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castPackBetrayal(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Does not scry without a Wolf or Werewolf")
    void noWolfDoesNotScry() {
        Permanent target = addTappedCreature(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castPackBetrayal(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Temporary control and haste expire at cleanup")
    void temporaryEffectsExpireAtCleanup() {
        Permanent target = addTappedCreature(player2);

        castPackBetrayal(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PacksBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addTappedCreature(Player player) {
        Permanent target = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        target.tap();
        return target;
    }

    private void castPackBetrayal(Permanent target) {
        harness.setHand(player1, List.of(new PacksBetrayal()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
