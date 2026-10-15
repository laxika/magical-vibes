package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWarringTriad.class, Forest.class, EnchantedEvening.class})
class TheWarringTriadTest extends BaseCardTest {

    @Test
    @DisplayName("Is not a creature with fewer than eight cards in its controller's graveyard")
    void isNotCreatureBelowGraveyardThreshold() {
        Permanent triad = addReadyTriad();

        assertThat(gqs.isCreature(gd, triad)).isFalse();
        assertThat(gqs.isArtifact(gd, triad)).isTrue();
    }

    @Test
    @DisplayName("Becomes a creature with eight cards in its controller's graveyard")
    void becomesCreatureAtGraveyardThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent triad = addReadyTriad();

        assertThat(gqs.isCreature(gd, triad)).isTrue();
    }

    @Test
    @DisplayName("Tapping mills a card and gives the target player one chosen mana")
    void millsAndAddsManaToTargetPlayer() {
        Permanent triad = addReadyTriad();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(triad.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(((PendingInteraction.ColorChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a permanent with its mana ability")
    void cannotTargetPermanent() {
        addReadyTriad();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsGraveyardDoesNotMakeItACreature() {
        harness.setGraveyard(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent triad = addReadyTriad();

        assertThat(gqs.isCreature(gd, triad)).isFalse();
    }

    @Test
    void millingEighthCardMakesItACreatureBeforeAbilityResolves() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent triad = addReadyTriad();
        assertThat(gqs.isCreature(gd, triad)).isFalse();

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(gqs.isCreature(gd, triad)).isTrue();
        assertThat(gqs.isArtifact(gd, triad)).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void stopsBeingCreatureWhenGraveyardFallsBelowEight() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent triad = addReadyTriad();
        assertThat(gqs.isCreature(gd, triad)).isTrue();

        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        assertThat(gqs.isCreature(gd, triad)).isFalse();
        assertThat(gqs.isArtifact(gd, triad)).isTrue();
    }

    @Test
    void cannotPayMillCostWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent triad = addReadyTriad();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(triad.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hasteAllowsActivationOnTurnItEntersAsCreature() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent triad = harness.addToBattlefieldAndReturn(player1, new TheWarringTriad());
        triad.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(triad.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void losingCreatureTypePreservesEnchantmentTypeGrantedEarlier() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent triad = addReadyTriad();

        assertThat(gqs.isCreature(gd, triad)).isFalse();
        assertThat(gqs.isArtifact(gd, triad)).isTrue();
        assertThat(gqs.isEnchantment(gd, triad)).isTrue();
    }

    private Permanent addReadyTriad() {
        return addCreatureReady(player1, new TheWarringTriad());
    }
}
