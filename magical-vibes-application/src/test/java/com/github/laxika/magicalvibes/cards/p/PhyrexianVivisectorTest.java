package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.y.YavimayaSteelcrusher;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianVivisector.class, YavimayaSteelcrusher.class, LightningStrike.class})
class PhyrexianVivisectorTest extends BaseCardTest {

    @Test
    @DisplayName("An ally creature dying causes you to scry 1")
    void allyCreatureDeathCausesScry() {
        harness.addToBattlefield(player1, new PhyrexianVivisector());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaSteelcrusher());
        harness.setLibrary(player1, List.of(new YavimayaSteelcrusher()));

        killWithLightningStrike(player1, creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Phyrexian Vivisector's own death causes you to scry 1")
    void ownDeathCausesScry() {
        Permanent vivisector = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        harness.setLibrary(player1, List.of(new YavimayaSteelcrusher()));

        killWithLightningStrike(player1, vivisector);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not cause you to scry")
    void opponentCreatureDeathDoesNotCauseScry() {
        harness.addToBattlefield(player1, new PhyrexianVivisector());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaSteelcrusher());

        killWithLightningStrike(player1, creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Scry may keep the top card without drawing it")
    void scryMayKeepTopCard() {
        Permanent vivisector = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        PhyrexianVivisector top = new PhyrexianVivisector();
        PhyrexianVivisector bottom = new PhyrexianVivisector();
        harness.setLibrary(player1, List.of(top, bottom));

        killWithLightningStrike(player1, vivisector);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry may put the top card on the bottom")
    void scryMayBottomTopCard() {
        Permanent vivisector = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        PhyrexianVivisector top = new PhyrexianVivisector();
        PhyrexianVivisector bottom = new PhyrexianVivisector();
        harness.setLibrary(player1, List.of(top, bottom));

        killWithLightningStrike(player1, vivisector);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry with an empty library resolves without a choice or a draw")
    void emptyLibraryDoesNotRequireChoice() {
        Permanent vivisector = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        harness.setLibrary(player1, List.of());

        killWithLightningStrike(player1, vivisector);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two Vivisectors dying together each scry once for each death")
    void simultaneousDeathsCreateFourSeparateScries() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        PhyrexianVivisector top = new PhyrexianVivisector();
        harness.setLibrary(player1, List.of(top));
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(4);
        for (int remaining = 3; remaining >= 0; remaining--) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            assertThat(gd.stack).hasSize(remaining);
        }
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killWithLightningStrike(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new LightningStrike()));
        harness.addMana(caster, ManaColor.RED, 2);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
