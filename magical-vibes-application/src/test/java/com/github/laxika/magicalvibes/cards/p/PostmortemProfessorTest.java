package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.m.MindRoots;
import com.github.laxika.magicalvibes.cards.q.QuickStudy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PostmortemProfessor.class, LastGasp.class, MindRoots.class, QuickStudy.class})
class PostmortemProfessorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes each opponent lose 1 life and its controller gain 1 life")
    void attackLifeSwing() {
        PostmortemProfessor card = new PostmortemProfessor();
        card.setPower(0);
        addCreatureReady(player1, card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The professor cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new PostmortemProfessor());
        Permanent professor = addCreatureReady(player2, new PostmortemProfessor());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(professor);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling an instant from the graveyard returns the professor to the battlefield")
    void exilesInstantAndReturnsToBattlefield() {
        PostmortemProfessor professor = new PostmortemProfessor();
        QuickStudy instant = new QuickStudy();
        harness.setGraveyard(player1, List.of(professor, instant));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(instant);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Postmortem Professor");
        harness.assertNotInGraveyard(player1, "Postmortem Professor");
    }

    @Test
    @DisplayName("The graveyard ability requires an instant or sorcery card")
    void requiresInstantOrSorceryInGraveyard() {
        harness.setGraveyard(player1, List.of(new PostmortemProfessor(), new PostmortemProfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
    }

    @Test
    @DisplayName("A sorcery pays the cost and only the activated professor returns, untapped")
    void sorceryCostReturnsOnlyActivatedCopy() {
        PostmortemProfessor professor = new PostmortemProfessor();
        PostmortemProfessor other = new PostmortemProfessor();
        MindRoots sorcery = new MindRoots();
        harness.setGraveyard(player1, List.of(professor, other, sorcery));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleGraveyardCardChosen(player1, 2);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sorcery);
        harness.assertNotOnBattlefield(player1, "Postmortem Professor");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(professor);
            assertThat(permanent.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("An opponent's instant cannot pay the graveyard activation cost")
    void cannotExileOpponentsInstantForCost() {
        harness.setGraveyard(player1, List.of(new PostmortemProfessor()));
        QuickStudy instant = new QuickStudy();
        harness.setGraveyard(player2, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("The activation requires black mana as well as one generic mana")
    void cannotActivateWithOnlyColorlessMana() {
        QuickStudy instant = new QuickStudy();
        harness.setGraveyard(player1, List.of(new PostmortemProfessor(), instant));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Postmortem Professor");
    }

    @Test
    @DisplayName("An older activation cannot return the professor after it returns and dies again")
    void oldActivationCannotReturnNewGraveyardObject() {
        PostmortemProfessor professor = new PostmortemProfessor();
        harness.setGraveyard(player1, List.of(professor, new QuickStudy(), new MindRoots()));
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Postmortem Professor");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Postmortem Professor"));
        harness.assertNotOnBattlefield(player1, "Postmortem Professor");
        harness.assertInGraveyard(player1, "Postmortem Professor");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Postmortem Professor");
        harness.assertInGraveyard(player1, "Postmortem Professor");
    }
}
