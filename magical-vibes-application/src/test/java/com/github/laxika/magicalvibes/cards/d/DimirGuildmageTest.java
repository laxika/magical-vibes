package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.z.ZephyrSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DimirGuildmage.class, ZephyrSpirit.class})
class DimirGuildmageTest extends BaseCardTest {

    @Test
    void targetPlayerDrawsACard() {
        harness.setLibrary(player2, List.of(new ZephyrSpirit()));
        prepareGuildmage(ManaColor.BLUE);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player2, "Zephyr Spirit");
    }

    @Test
    void targetPlayerDiscardsACard() {
        harness.setHand(player2, List.of(new ZephyrSpirit()));
        prepareGuildmage(ManaColor.BLACK);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Zephyr Spirit");
    }

    @Test
    void bothAbilitiesCanBeActivatedWithoutTapping() {
        harness.setLibrary(player2, List.of(new ZephyrSpirit()));
        harness.setHand(player2, List.of());
        Permanent guildmage = prepareGuildmage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Zephyr Spirit");
    }

    @Test
    void targetPlayerCanBeTheControllerForBothAbilities() {
        harness.setLibrary(player1, List.of(new ZephyrSpirit()));
        harness.setHand(player1, List.of());
        prepareGuildmage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Zephyr Spirit");

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Zephyr Spirit");
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent guildmage = prepareGuildmage(ManaColor.BLUE);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guildmage.isTapped()).isFalse();
    }

    @Test
    void discardAbilityCannotTargetAPermanent() {
        Permanent guildmage = prepareGuildmage(ManaColor.BLACK);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guildmage.isTapped()).isFalse();
    }

    @Test
    void abilitiesCanOnlyBeActivatedAsSorceries() {
        prepareGuildmage(ManaColor.BLUE);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardAbilityCanOnlyBeActivatedAsASorcery() {
        prepareGuildmage(ManaColor.BLACK);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothAbilitiesAreUnavailableDuringOpponentsMainPhase() {
        prepareGuildmage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherAbilityCanBeActivatedWithAnAbilityOnTheStack() {
        harness.setLibrary(player2, List.of(new ZephyrSpirit()));
        prepareGuildmage();
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Zephyr Spirit");
    }

    @Test
    void bothAbilitiesWorkWhileTappedAndSummoningSickInPostcombatMainPhase() {
        harness.setLibrary(player2, List.of(new ZephyrSpirit()));
        harness.setHand(player2, List.of());
        Permanent guildmage = prepareGuildmage();
        guildmage.setTapped(true);
        guildmage.setSummoningSick(true);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Zephyr Spirit");
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Zephyr Spirit");
    }

    @Test
    void discardAgainstAnEmptyHandResolvesWithoutAChoice() {
        harness.setHand(player2, List.of());
        prepareGuildmage(ManaColor.BLACK);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetedPlayerChoosesExactlyOneCardToDiscard() {
        ZephyrSpirit kept = new ZephyrSpirit();
        ZephyrSpirit discarded = new ZephyrSpirit();
        harness.setHand(player2, List.of(kept, discarded));
        prepareGuildmage(ManaColor.BLACK);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    private Permanent prepareGuildmage(ManaColor coloredMana) {
        Permanent guildmage = prepareGuildmage();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, coloredMana, 1);
        return guildmage;
    }

    private Permanent prepareGuildmage() {
        Permanent guildmage = addCreatureReady(player1, new DimirGuildmage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return guildmage;
    }
}
