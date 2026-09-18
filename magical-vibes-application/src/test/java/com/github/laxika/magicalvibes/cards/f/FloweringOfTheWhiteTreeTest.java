package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({FloweringOfTheWhiteTree.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, Shock.class})
class FloweringOfTheWhiteTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs legendary and nonlegendary creatures you control")
    void buffsOwnLegendaryAndNonlegendaryCreatures() {
        harness.addToBattlefield(player1, new FloweringOfTheWhiteTree());
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonlegendary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nonlegendary)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not affect creatures controlled by an opponent")
    void doesNotAffectOpponentsCreatures() {
        harness.addToBattlefield(player1, new FloweringOfTheWhiteTree());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        Permanent opponentNonlegendary = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentLegendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentLegendary)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentNonlegendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentNonlegendary)).isEqualTo(2);
    }

    @Test
    @DisplayName("Legendary creatures you control have ward {1}")
    void grantsWardToOwnLegendaryCreatures() {
        harness.addToBattlefield(player1, new FloweringOfTheWhiteTree());
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, legendary.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Nonlegendary creatures you control do not have ward from Flowering of the White Tree")
    void doesNotGrantWardToOwnNonlegendaryCreatures() {
        harness.addToBattlefield(player1, new FloweringOfTheWhiteTree());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, nonlegendary.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(nonlegendary.getMarkedDamage()).isEqualTo(2);
    }
}
