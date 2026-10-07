package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.DevkarinDissident;
import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrostaniDiscordant.class, DevkarinDissident.class, MindControl.class, ActOfTreason.class})
class TrostaniDiscordantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void boostsOtherOwnCreatures() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new DevkarinDissident());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Trostani enters, it creates two Soldier tokens with lifelink")
    void createsSoldierTokensWithLifelink() {
        castTrostani();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier ->
                assertThat(gqs.hasKeyword(gd, soldier, Keyword.LIFELINK)).isTrue());
    }

    @Test
    @DisplayName("At the beginning of the controller's end step, each player gets their owned creatures back")
    void returnsOwnedCreaturesToTheirOwners() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());

        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        gd.stolenCreatures.put(playerOneCreature.getId(), player1.getId());
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player1, new DevkarinDissident());
        gd.stolenCreatures.put(playerTwoCreature.getId(), player2.getId());

        resolveControllerEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(playerOneCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(playerTwoCreature);
    }

    @Test
    @DisplayName("Soldiers are boosted while Trostani is present and remain 1/1 with lifelink after it leaves")
    void tokensKeepTheirCharacteristicsAfterTrostaniLeaves() {
        castTrostani();
        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
        });

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Trostani Discordant"));

        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, soldier, Keyword.LIFELINK)).isTrue();
        });
    }

    @Test
    @DisplayName("Trostani does not return creatures at the opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The end-step ability resolves even if Trostani leaves the battlefield in response")
    void endStepTriggerSurvivesSourceLeaving() {
        Permanent trostani = harness.addToBattlefieldAndReturn(player1, new TrostaniDiscordant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevkarinDissident());
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(trostani);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The enter trigger creates tokens even if Trostani leaves before it resolves")
    void enterTriggerSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new TrostaniDiscordant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Trostani Discordant"));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier -> {
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, soldier, Keyword.LIFELINK)).isTrue();
        });
    }

    @Test
    @DisplayName("Trostani overrides Mind Control without removing the Aura")
    void overridesExistingControlAura() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevkarinDissident());
        harness.setHand(player2, List.of(new MindControl()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        resolveControllerEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertOnBattlefield(player2, "Mind Control");
        assertThat(findPermanent(player2, "Mind Control").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An owner who temporarily regained a creature keeps it after cleanup")
    void establishesLastingControlForCreaturesAlreadyControlledByOwner() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());
        DevkarinDissident card = new DevkarinDissident();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player2, List.of(new MindControl()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        resolveControllerEndStep();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    private void castTrostani() {
        harness.setHand(player1, List.of(new TrostaniDiscordant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
