package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.n.NecroticSliver;
import com.github.laxika.magicalvibes.cards.o.OrazcaRelic;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeySteed.class, GrizzlyBears.class, Shock.class, JaceBeleren.class,
        NecroticSliver.class, OrazcaRelic.class, TurnToFrog.class})
class FeySteedTest extends BaseCardTest {

    @Test
    @DisplayName("Another attacking creature you control gains indestructible")
    void protectsAnotherAttackingCreature() {
        addCreatureReady(player1, new FeySteed());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target Fey Steed itself")
    void cannotTargetItself() {
        Permanent steed = addCreatureReady(player1, new FeySteed());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, steed.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FeySteed());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("May draw when an opponent targets a creature you control")
    void mayDrawWhenOpponentTargetsCreature() {
        harness.addToBattlefield(player1, new FeySteed());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        castOpponentShock(creature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("May draw when an opponent targets a planeswalker you control")
    void mayDrawWhenOpponentTargetsPlaneswalker() {
        harness.addToBattlefield(player1, new FeySteed());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        castOpponentShock(planeswalker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger when you target your own permanent")
    void doesNotTriggerOnYourOwnTargetingSpell() {
        harness.addToBattlefield(player1, new FeySteed());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent targets the controller")
    void doesNotTriggerWhenControllerIsTargeted() {
        harness.addToBattlefield(player1, new FeySteed());

        castOpponentShock(player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent's ability targets a noncreature artifact")
    void doesNotTriggerWhenNoncreatureArtifactIsTargeted() {
        harness.addToBattlefield(player1, new FeySteed());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OrazcaRelic());
        harness.addToBattlefield(player2, new NecroticSliver());

        activateOpponentSliver(artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("May draw when an opponent's activated ability targets Fey Steed itself")
    void mayDrawWhenOpponentAbilityTargetsSteed() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new FeySteed());
        harness.addToBattlefield(player2, new NecroticSliver());
        harness.setLibrary(player1, List.of(new FeySteed()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateOpponentSliver(steed.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The controller may decline the draw")
    void mayDeclineDraw() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new FeySteed());
        harness.addToBattlefield(player2, new NecroticSliver());
        harness.setLibrary(player1, List.of(new FeySteed()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateOpponentSliver(steed.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent targets their own creature")
    void doesNotTriggerWhenOpponentCreatureIsTargeted() {
        harness.addToBattlefield(player1, new FeySteed());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeySteed());

        castOpponentShock(creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot protect a creature that is not attacking")
    void cannotTargetNonattackingCreature() {
        addCreatureReady(player1, new FeySteed());
        addCreatureReady(player1, new FeySteed());
        Permanent nonattacker = addCreatureReady(player1, new FeySteed());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot protect an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new FeySteed());
        addCreatureReady(player1, new FeySteed());
        Permanent opponentCreature = addCreatureReady(player2, new FeySteed());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw after Fey Steed loses its abilities")
    void doesNotTriggerAfterLosingAbilities() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new FeySteed());
        harness.addToBattlefield(player2, new NecroticSliver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, steed.getId());

        activateOpponentSliver(steed.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private void activateOpponentSliver(java.util.UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, targetId);
    }

    private void castOpponentShock(java.util.UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
    }
}
