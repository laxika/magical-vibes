package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContractHero.class, Ornithopter.class, Shock.class})
class ContractHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Treasure token")
    void enteringCreatesTreasure() {
        harness.setHand(player1, List.of(new ContractHero()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing an artifact gives Contract Hero +2/+0 until end of turn")
    void sacrificingArtifactBoostsHero() {
        harness.setHand(player1, List.of());
        Permanent hero = addCreatureReady(player1, new ContractHero());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        attackAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
    }

    @Test
    @DisplayName("Discarding a card gives Contract Hero +2/+0 until end of turn")
    void discardingCardBoostsHero() {
        Shock discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        Permanent hero = addCreatureReady(player1, new ContractHero());

        attackAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Declining the attack trigger does nothing")
    void decliningDoesNothing() {
        Shock card = new Shock();
        harness.setHand(player1, List.of(card));
        Permanent hero = addCreatureReady(player1, new ContractHero());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    @Test
    void canChooseSacrificeWhenDiscardIsAlsoAvailable() {
        Shock card = new Shock();
        harness.setHand(player1, List.of(card));
        Permanent hero = addCreatureReady(player1, new ContractHero());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        attackAndAcceptMay();
        harness.handleListChoice(player1, "Sacrifice an artifact");
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    void canChooseDiscardWhenSacrificeIsAlsoAvailable() {
        Shock card = new Shock();
        harness.setHand(player1, List.of(card));
        Permanent hero = addCreatureReady(player1, new ContractHero());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        attackAndAcceptMay();
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void acceptingWithoutAnArtifactOrCardDoesNotBoostHero() {
        harness.setHand(player1, List.of());
        Permanent hero = addCreatureReady(player1, new ContractHero());
        addCreatureReady(player2, new Ornithopter());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::attackAndAcceptMay);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canSacrificeTheTreasureCreatedOnEntering() {
        harness.setHand(player1, List.of(new ContractHero()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent hero = findPermanent(player1, "Contract Hero");
        hero.setSummoningSick(false);
        Permanent treasure = findPermanent(player1, "Treasure");

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, treasure.getId());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void attackAndAcceptMay() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }
}
