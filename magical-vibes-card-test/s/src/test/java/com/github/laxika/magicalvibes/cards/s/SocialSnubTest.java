package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SocialSnub.class, GrizzlyBears.class})
class SocialSnubTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature for each player and drains each opponent")
    void sacrificesCreaturesAndDrainsOpponent() {
        addCreatureReady(player2, new GrizzlyBears());
        castSocialSnub();
        addCreatureReady(player1, new GrizzlyBears());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Offers the copy only while the caster controls a creature")
    void offersCopyWhenCasterControlsCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castSocialSnub();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An accepted copy resolves before the original spell")
    void acceptedCopyResolvesBeforeOriginal() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castSocialSnub();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    private void castSocialSnub() {
        harness.castFromHand(player1, new SocialSnub(), "{1}{W}{B}");
    }

    @Test
    @DisplayName("Life changes still happen when neither player controls a creature")
    void drainsWithoutCreatures() {
        castSocialSnub();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Social Snub");
    }

    @Test
    @DisplayName("Both players choose a creature before simultaneous sacrifices and life changes")
    void playersChooseBeforeSacrifices() {
        var firstChoice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var firstSurvivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var secondChoice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var secondSurvivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSocialSnub();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(firstChoice.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstChoice, firstSurvivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondChoice, secondSurvivor);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleMultiplePermanentsChosen(player2, List.of(secondChoice.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(firstSurvivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(secondSurvivor);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }
}
