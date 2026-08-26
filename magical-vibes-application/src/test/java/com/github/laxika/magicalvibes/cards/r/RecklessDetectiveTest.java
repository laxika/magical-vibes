package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessDetective.class, Ornithopter.class, Shock.class, GrizzlyBears.class})
class RecklessDetectiveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact draws and boosts Reckless Detective")
    void sacrificingArtifactDrawsAndBoosts() {
        Shock drawn = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        attackAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Discarding a card draws and boosts Reckless Detective")
    void discardingCardDrawsAndBoosts() {
        Shock discarded = new Shock();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        Permanent detective = addCreatureReady(player1, new RecklessDetective());

        attackAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("When both costs are available, the controller chooses one")
    void choosesBetweenSacrificeAndDiscard() {
        harness.setHand(player1, List.of(new Shock()));
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        attackAndAcceptMay();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Sacrifice an artifact", "Discard a card");
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Declining the attack trigger does nothing")
    void decliningDoesNothing() {
        Shock card = new Shock();
        harness.setHand(player1, List.of(card));
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, detective)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    private void attackAndAcceptMay() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }
}
