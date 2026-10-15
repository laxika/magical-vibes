package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.g.GoblinHeelcutter;
import com.github.laxika.magicalvibes.cards.j.JeskaiSage;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingChampion.class, ArashinCleric.class, GoblinHeelcutter.class, JeskaiSage.class})
class WanderingChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage enables the discard-and-draw ability with a blue permanent")
    void bluePermanentEnablesDiscardAndDraw() {
        harness.addToBattlefield(player1, new JeskaiSage());
        harness.setLibrary(player1, List.of(new GoblinHeelcutter()));
        harness.setHand(player1, List.of(new ArashinCleric()));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Arashin Cleric");
        harness.assertInHand(player1, "Goblin Heelcutter");
    }

    @Test
    @DisplayName("Combat damage enables the discard-and-draw ability with a red permanent")
    void redPermanentEnablesDiscardAndDraw() {
        harness.addToBattlefield(player1, new GoblinHeelcutter());
        harness.setHand(player1, List.of(new ArashinCleric()));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Arashin Cleric");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage does not trigger without a blue or red permanent")
    void noColoredPermanentDoesNotTrigger() {
        harness.setHand(player1, List.of(new ArashinCleric()));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Arashin Cleric");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's blue or red permanents do not enable the ability")
    void opponentsColoredPermanentsDoNotEnableAbility() {
        harness.addToBattlefield(player2, new JeskaiSage());
        harness.addToBattlefield(player2, new GoblinHeelcutter());
        harness.setHand(player1, List.of(new ArashinCleric()));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Arashin Cleric");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty hand does not draw a card")
    void emptyHandDoesNotDraw() {
        harness.addToBattlefield(player1, new GoblinHeelcutter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ArashinCleric()));

        attackWithChampionDealingDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability does nothing if the last qualifying permanent leaves before resolution")
    void conditionIsRecheckedOnResolution() {
        harness.addToBattlefield(player1, new GoblinHeelcutter());
        Permanent redPermanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ArashinCleric()));
        harness.setLibrary(player1, List.of(new JeskaiSage()));
        dealDamageWithoutResolvingTrigger();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(redPermanent);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Arashin Cleric");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controlling both colors creates one trigger and draws only one card")
    void bothColorsCreateOneTrigger() {
        harness.addToBattlefield(player1, new JeskaiSage());
        harness.addToBattlefield(player1, new GoblinHeelcutter());
        harness.setHand(player1, List.of(new ArashinCleric()));
        harness.setLibrary(player1, List.of(new GoblinHeelcutter(), new JeskaiSage()));
        dealDamageWithoutResolvingTrigger();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Arashin Cleric");
        harness.assertInHand(player1, "Goblin Heelcutter");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void dealDamageWithoutResolvingTrigger() {
        Permanent champion = addCreatureReady(player1, new WanderingChampion());
        champion.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
    }

    private void attackWithChampionDealingDamage() {
        Permanent champion = addCreatureReady(player1, new WanderingChampion());
        champion.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
