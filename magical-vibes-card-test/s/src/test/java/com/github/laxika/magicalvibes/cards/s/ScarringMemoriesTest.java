package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.f.FrogSquirrels;
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

@CardUsed({ScarringMemories.class, CaptainSisay.class, FrogSquirrels.class})
class ScarringMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent sacrifices a creature, discards a card, and loses 3 life")
    void resolvesAllEffects() {
        harness.addToBattlefield(player2, new FrogSquirrels());
        harness.setHand(player2, List.of(new ScarringMemories()));
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player2, "Frog-Squirrels");
        harness.assertInGraveyard(player2, "Frog-Squirrels");
        harness.assertInGraveyard(player2, "Scarring Memories");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can be cast as though it had flash while controlling an attacking legendary creature")
    void attackingLegendaryCreatureGrantsFlash() {
        Permanent legendary = addCreatureReady(player1, new CaptainSisay());
        legendary.setAttacking(true);
        prepareCombatCast();

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast with no attacking legendary creature")
    void cannotCastWithoutAttackingLegendaryCreature() {
        prepareOpponentTurnCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent chooses the sacrificed creature and the discarded card before losing life")
    void opponentChoosesCreatureAndCard() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FrogSquirrels());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FrogSquirrels());
        harness.setHand(player2, List.of(new ScarringMemories(), new CaptainSisay()));
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Captain Sisay");
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player2, "Frog-Squirrels");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 1);

        harness.assertInHand(player2, "Scarring Memories");
        harness.assertNotInHand(player2, "Captain Sisay");
        harness.assertInGraveyard(player2, "Captain Sisay");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent still discards and loses life when they control no creatures")
    void noCreaturesStillDiscardsAndLosesLife() {
        harness.setHand(player2, List.of(new CaptainSisay()));
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Captain Sisay");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Opponent still sacrifices and loses life with an empty hand")
    void emptyHandStillSacrificesAndLosesLife() {
        harness.addToBattlefield(player2, new FrogSquirrels());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Frog-Squirrels");
        harness.assertInGraveyard(player2, "Frog-Squirrels");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Opponent with no creatures or cards still loses 3 life")
    void emptyBattlefieldAndHandStillLosesLife() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Scarring Memories");
    }

    @Test
    @DisplayName("Cannot target the spell's controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A legendary creature that is not attacking does not grant flash")
    void nonattackingLegendaryCreatureDoesNotGrantFlash() {
        addCreatureReady(player1, new CaptainSisay());
        prepareCombatCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking nonlegendary creature does not grant flash")
    void attackingNonlegendaryCreatureDoesNotGrantFlash() {
        Permanent attacker = addCreatureReady(player1, new FrogSquirrels());
        attacker.setAttacking(true);
        prepareCombatCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's attacking legendary creature does not grant flash")
    void opponentsAttackingLegendaryCreatureDoesNotGrantFlash() {
        Permanent attacker = addCreatureReady(player2, new CaptainSisay());
        attacker.setAttacking(true);
        prepareOpponentTurnCast();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing the flash condition after casting does not stop resolution")
    void flashConditionOnlyRequiredWhenCasting() {
        Permanent attacker = addCreatureReady(player1, new CaptainSisay());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of());
        prepareCombatCast();
        harness.castSorcery(player1, 0, player2.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Scarring Memories");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void prepareOpponentTurnCast() {
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void prepareCombatCast() {
        harness.setHand(player1, List.of(new ScarringMemories()));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
