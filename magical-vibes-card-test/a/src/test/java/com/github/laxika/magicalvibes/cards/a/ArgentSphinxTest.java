package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgentSphinx.class, Memnite.class})
class ArgentSphinxTest extends BaseCardTest {


    @Test
    @DisplayName("Cannot activate ability without three artifacts")
    void cannotActivateWithoutThreeArtifacts() {
        addSphinxReady(player1);
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    @DisplayName("Can activate ability with three artifacts")
    void canActivateWithThreeArtifacts() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Sphinx should be exiled
        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Argent Sphinx"));
    }


    @Test
    @DisplayName("Sphinx is exiled when ability resolves")
    void sphinxIsExiledOnResolve() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Argent Sphinx"));
    }

    @Test
    @DisplayName("Sphinx returns to battlefield at beginning of next end step")
    void sphinxReturnsAtEndStep() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Sphinx is exiled
        harness.assertNotOnBattlefield(player1, "Argent Sphinx");

        // Advance to end step naturally (POSTCOMBAT_MAIN -> END_STEP triggers handler)
        advanceToEndStep();

        // Sphinx should be back on the battlefield
        harness.assertOnBattlefield(player1, "Argent Sphinx");
        // And removed from exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Argent Sphinx"));
    }

    @Test
    @DisplayName("Returned Sphinx has summoning sickness")
    void returnedSphinxHasSummoningSickness() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        Permanent returnedSphinx = findPermanent(player1, "Argent Sphinx");
        assertThat(returnedSphinx.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returned Sphinx is under controller's control")
    void returnedSphinxUnderControllerControl() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Argent Sphinx");
        harness.assertNotOnBattlefield(player2, "Argent Sphinx");
    }


    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when ability is activated")
    void manaConsumedOnActivation() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }


    @Test
    void returnUsesTheStackAtTheNextEndStep() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Argent Sphinx");
    }

    @Test
    void opponentsArtifactsDoNotEnableMetalcraft() {
        addSphinxReady(player1);
        addThreeArtifacts(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    void stolenSphinxReturnsUnderTheActivatingPlayersControl() {
        ArgentSphinx card = new ArgentSphinx();
        card.setOwnerId(player2.getId());
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(sphinx.getId(), player2.getId());
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Argent Sphinx");
        harness.assertNotOnBattlefield(player2, "Argent Sphinx");
    }

    @Test
    void losingMetalcraftAfterActivationDoesNotStopExileOrReturn() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Memnite);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        advanceToEndStep();
        harness.assertOnBattlefield(player1, "Argent Sphinx");
    }

    @Test
    void canActivateWhileSummoningSickAndTapped() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new ArgentSphinx());
        sphinx.setSummoningSick(true);
        sphinx.tap();
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        advanceToEndStep();
        assertThat(findPermanent(player1, "Argent Sphinx").isTapped()).isFalse();
    }

    @Test
    void activationDuringEndStepWaitsForTheFollowingEndStep() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Argent Sphinx");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Argent Sphinx");
    }

    private Permanent addSphinxReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ArgentSphinx());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addThreeArtifacts(Player player) {
        harness.addToBattlefield(player, new Memnite());
        harness.addToBattlefield(player, new Memnite());
        harness.addToBattlefield(player, new Memnite());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
