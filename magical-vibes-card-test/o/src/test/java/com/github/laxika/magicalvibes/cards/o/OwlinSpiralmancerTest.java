package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvyElemental;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OwlinSpiralmancer.class, GrizzlyBears.class, IvyElemental.class, MindSpring.class, EntrancingMelody.class})
class OwlinSpiralmancerTest extends BaseCardTest {

    @Test
    @DisplayName("May copy the first X spell and preserves its X value")
    void copiesFirstXSpell() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 1);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A copied permanent X spell becomes a token")
    void copiedPermanentSpellBecomesToken() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, 2);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ivy Elemental"))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Only the first X spell each turn can be copied")
    void onlyCopiesFirstXSpellEachTurn() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new MindSpring(), new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.castSorcery(player1, 0, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A non-X spell does not trigger Owlin Spiralmancer")
    void nonXSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the first copy does not allow copying the second X spell")
    void decliningStillConsumesFirstXSpell() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new MindSpring(), new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X equal to zero still qualifies as the first X spell")
    void zeroXStillTriggers() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An X spell cast before Spiralmancer enters still counts as the first")
    void earlierXSpellCountsBeforeSourceEnters() {
        harness.setHand(player1, List.of(new MindSpring(), new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveSorcery(player1, 0, 1);
        harness.addToBattlefield(player1, new OwlinSpiralmancer());

        harness.castSorcery(player1, 0, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting a non-X spell first does not consume the X spell trigger")
    void nonXSpellDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new GrizzlyBears(), new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's X spell does not trigger your Spiralmancer")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MindSpring()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The copy may gain control of a different creature while the original keeps its target")
    void copyCanChooseNewTarget() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new OwlinSpiralmancer());
        var copyTarget = harness.addToBattlefieldAndReturn(player2, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 4, originalTarget.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(originalTarget, copyTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(originalTarget, copyTarget);
    }
}
