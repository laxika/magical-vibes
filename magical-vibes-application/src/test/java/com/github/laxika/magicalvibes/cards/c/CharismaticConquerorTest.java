package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JestersMask;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CharismaticConqueror.class, GrizzlyBears.class, JestersMask.class, SolRing.class})
class CharismaticConquerorTest extends BaseCardTest {

    @Test
    @DisplayName("The entering opponent may tap their untapped creature instead of creating a Vampire")
    void opponentMayTapEnteringCreature() {
        harness.addToBattlefield(player1, new CharismaticConqueror());

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bears.isTapped()).isTrue();
        assertThat(findVampires(player1)).isEmpty();
    }

    @Test
    @DisplayName("Declining to tap creates a 1/1 lifelink Vampire for the Conqueror's controller")
    void decliningToTapCreatesVampire() {
        harness.addToBattlefield(player1, new CharismaticConqueror());

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(findVampires(player1)).singleElement()
                .satisfies(vampire -> assertThat(vampire.getCard().getKeywords()).contains(Keyword.LIFELINK));
        assertThat(findVampires(player2)).isEmpty();
    }

    @Test
    @DisplayName("The ability ignores your own entries and opponent permanents that enter tapped")
    void ignoresOwnAndTappedEntries() {
        harness.addToBattlefield(player1, new CharismaticConqueror());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        Permanent mask = harness.enterBattlefieldAndReturn(player2, new JestersMask());
        assertThat(mask.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findVampires(player1)).isEmpty();
    }

    @Test
    @DisplayName("An untapped opponent artifact can be tapped instead of creating a Vampire")
    void opponentMayTapEnteringArtifact() {
        harness.addToBattlefield(player1, new CharismaticConqueror());

        Permanent ring = harness.enterBattlefieldAndReturn(player2, new SolRing());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ring.isTapped()).isTrue();
        assertThat(findVampires(player1)).isEmpty();
    }

    @Test
    @DisplayName("An artifact tapped for mana in response forces creation of a Vampire")
    void alreadyTappedPermanentCreatesVampire() {
        harness.addToBattlefield(player1, new CharismaticConqueror());
        Permanent ring = harness.enterBattlefieldAndReturn(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);
        assertThat(ring.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findVampires(player1)).hasSize(1);
    }

    @Test
    @DisplayName("A permanent that left before resolution forces creation of a Vampire")
    void removedPermanentCreatesVampire() {
        harness.addToBattlefield(player1, new CharismaticConqueror());
        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findVampires(player1)).hasSize(1);
    }

    private java.util.List<Permanent> findVampires(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Vampire"))
                .toList();
    }
}
