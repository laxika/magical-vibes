package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DimirGuildmage.class, GrizzlyBears.class})
class DimirGuildmageTest extends BaseCardTest {

    @Test
    void targetPlayerDrawsACard() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        prepareGuildmage(ManaColor.BLUE);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void targetPlayerDiscardsACard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        prepareGuildmage(ManaColor.BLACK);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void bothAbilitiesCanBeActivatedWithoutTapping() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        Permanent guildmage = prepareGuildmage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent guildmage = prepareGuildmage(ManaColor.BLUE);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
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
