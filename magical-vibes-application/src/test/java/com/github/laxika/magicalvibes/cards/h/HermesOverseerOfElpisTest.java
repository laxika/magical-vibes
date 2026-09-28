package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HermesOverseerOfElpis.class, GrizzlyBears.class, Shock.class, SuntailHawk.class})
class HermesOverseerOfElpisTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a blue Bird with flying and vigilance")
    void noncreatureSpellCreatesBird() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bird, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Bird")
    void creatureSpellCreatesNoBird() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    @DisplayName("Attacking with one or more Birds scries two")
    void attackingWithBirdsScriesTwo() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player1, new SuntailHawk());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Attacking without a Bird does not scry")
    void attackingWithoutBirdDoesNotScry() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
