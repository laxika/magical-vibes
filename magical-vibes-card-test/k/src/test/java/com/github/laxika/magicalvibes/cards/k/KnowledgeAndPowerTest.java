package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesSanctum;
import com.github.laxika.magicalvibes.cards.s.SigiledStarfish;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnowledgeAndPower.class, AngelsMercy.class, GrizzlyBears.class,
        JacesSanctum.class, SigiledStarfish.class})
class KnowledgeAndPowerTest extends BaseCardTest {

    @Test
    @DisplayName("After scrying, paying {2} deals 2 damage to the chosen player")
    void payingDealsDamageToPlayer() {
        prepareGame();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        triggerKnowledgeAndPower();
        chooseScryTopCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the payment prevents the damage")
    void decliningPreventsDamage() {
        prepareGame();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        triggerKnowledgeAndPower();
        chooseScryTopCard();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The paid trigger can target a creature")
    void payingDealsDamageToCreature() {
        prepareGame();
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        triggerKnowledgeAndPower();
        chooseScryTopCard();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    private void prepareGame() {
        harness.addToBattlefield(player1, new KnowledgeAndPower());
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void triggerKnowledgeAndPower() {
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    private void chooseScryTopCard() {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Accepting without enough mana does not deal damage")
    void insufficientManaPreventsDamage() {
        prepareGame();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        triggerKnowledgeAndPower();
        chooseScryTopCard();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({KnowledgeAndPower.class, SigiledStarfish.class})
    @DisplayName("Scrying an empty library still triggers Knowledge and Power")
    void emptyLibraryStillTriggers() {
        harness.addToBattlefield(player1, new KnowledgeAndPower());
        var starfish = harness.addToBattlefieldAndReturn(player1, new SigiledStarfish());
        starfish.setSummoningSick(false);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({KnowledgeAndPower.class, SigiledStarfish.class})
    @DisplayName("An opponent's scry does not trigger Knowledge and Power")
    void opponentsScryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KnowledgeAndPower());
        var starfish = harness.addToBattlefieldAndReturn(player2, new SigiledStarfish());
        starfish.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new SigiledStarfish()));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({KnowledgeAndPower.class, SigiledStarfish.class})
    @DisplayName("Bottoming a scried card triggers and can damage the controller")
    void bottomingTriggersAndCanTargetController() {
        harness.addToBattlefield(player1, new KnowledgeAndPower());
        var starfish = harness.addToBattlefieldAndReturn(player1, new SigiledStarfish());
        starfish.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new SigiledStarfish()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
