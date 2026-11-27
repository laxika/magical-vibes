package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoldeviHeretic.class, LeylineOfSanctity.class})
class SoldeviHereticTest extends BaseCardTest {

    private void activate(UUID creatureId, Player opponent) {
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creatureId, opponent.getId()));
    }

    private Permanent addHereticReady() {
        Permanent heretic = addCreatureReady(player1, new SoldeviHeretic());
        harness.addMana(player1, ManaColor.WHITE, 1);
        return heretic;
    }

    @Test
    @DisplayName("Shields the target creature for 2 and offers the opponent a card")
    void shieldsTargetAndOffersDraw() {
        addHereticReady();
        harness.addToBattlefield(player2, new SoldeviHeretic());
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        UUID targetId = harness.getPermanentId(player2, "Soldevi Heretic");
        activate(targetId, player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Soldevi Heretic");
        assertThat(gd.permanentDamagePreventionShield(target.getId())).isEqualTo(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The opponent chooses whether to draw as the ability resolves, with nothing put on the stack")
    void opponentChoosesAsTheAbilityResolves() {
        addHereticReady();
        harness.addToBattlefield(player2, new SoldeviHeretic());
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        UUID targetId = harness.getPermanentId(player2, "Soldevi Heretic");
        activate(targetId, player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The opponent may decline the draw")
    void opponentMayDecline() {
        addHereticReady();
        harness.addToBattlefield(player2, new SoldeviHeretic());
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        UUID targetId = harness.getPermanentId(player2, "Soldevi Heretic");
        activate(targetId, player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("The controller never draws from the ability")
    void controllerDoesNotDraw() {
        addHereticReady();
        harness.addToBattlefield(player2, new SoldeviHeretic());
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        UUID targetId = harness.getPermanentId(player2, "Soldevi Heretic");
        activate(targetId, player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(controllerHandBefore);
    }

    @Test
    @DisplayName("Cannot target a player with the prevention half")
    void cannotTargetPlayerWithPrevention() {
        addHereticReady();

        assertThatThrownBy(() -> activate(player2.getId(), player2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target its controller as the opponent")
    void cannotTargetControllerAsOpponent() {
        Permanent heretic = addHereticReady();

        assertThatThrownBy(() -> activate(heretic.getId(), player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With two opponents, the targeted opponent is the one offered the card")
    void targetedOpponentIsOfferedTheCard() {
        Player player3 = harness.addOpponent("Player3");
        Permanent heretic = addHereticReady();
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));
        harness.setLibrary(player3, List.of(new SoldeviHeretic()));

        activate(heretic.getId(), player3);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player3.getId());
        int handBefore = gd.playerHands.get(player3.getId()).size();
        harness.handleMayAbilityChosen(player3, true);
        assertThat(gd.playerHands.get(player3.getId())).hasSize(handBefore + 1);
        assertThat(gd.permanentDamagePreventionShield(heretic.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent who became an illegal target is offered nothing, and the creature is still shielded")
    void illegalOpponentTargetIsOfferedNothing() {
        Permanent heretic = addHereticReady();
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        activate(heretic.getId(), player2);
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.permanentDamagePreventionShield(heretic.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be activated without the {W} in the cost")
    void requiresWhiteMana() {
        addCreatureReady(player1, new SoldeviHeretic());
        harness.addToBattlefield(player2, new SoldeviHeretic());

        UUID targetId = harness.getPermanentId(player2, "Soldevi Heretic");
        assertThatThrownBy(() -> activate(targetId, player2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents the next 2 damage to the targeted creature")
    void preventsNextTwoDamageToTargetCreature() {
        Permanent source = addHereticReady();
        Permanent attacker = addCreatureReady(player1, new SoldeviHeretic());
        Permanent target = addCreatureReady(player2, new SoldeviHeretic());

        activate(target.getId(), player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(source.isTapped()).isTrue();

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.permanentDamagePreventionShield(target.getId())).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("The opponent may still draw when the creature target leaves the battlefield")
    void opponentMayDrawWhenCreatureTargetLeaves() {
        addHereticReady();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoldeviHeretic());
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        activate(target.getId(), player2);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The Heretic can protect itself while offering the opponent the draw")
    void canTargetItself() {
        Permanent heretic = addHereticReady();
        harness.setLibrary(player2, List.of(new SoldeviHeretic()));

        activate(heretic.getId(), player2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(heretic.isTapped()).isTrue();
        assertThat(gd.permanentDamagePreventionShield(heretic.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("A summoning-sick Heretic cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent heretic = harness.addToBattlefieldAndReturn(player1, new SoldeviHeretic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> activate(heretic.getId(), player2))
                .isInstanceOf(IllegalStateException.class);
    }
}
