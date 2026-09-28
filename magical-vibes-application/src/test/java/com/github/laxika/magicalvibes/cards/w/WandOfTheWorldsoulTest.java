package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WandOfTheWorldsoul.class, GrizzlyBears.class, SolRing.class})
class WandOfTheWorldsoulTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for white mana")
    void entersTappedAndTapsForWhiteMana() {
        harness.setHand(player1, List.of(new WandOfTheWorldsoul()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent wand = findPermanent(player1, "Wand of the Worldsoul");
        assertThat(wand.isTapped()).isTrue();

        wand.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives the next spell convoke and consumes the grant")
    void grantsConvokeToNextSpell() {
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        SolRing firstSolRing = new SolRing();
        harness.setHand(player1, List.of(firstSolRing));
        UUID creatureId = creature.getId();
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creatureId));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        creature.untap();
        harness.setHand(player1, List.of(new SolRing()));
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
