package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TheWarringTriad.class, Forest.class})
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
        harness.handleListChoice(player1, "GREEN");

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

    private Permanent addReadyTriad() {
        return addCreatureReady(player1, new TheWarringTriad());
    }
}
