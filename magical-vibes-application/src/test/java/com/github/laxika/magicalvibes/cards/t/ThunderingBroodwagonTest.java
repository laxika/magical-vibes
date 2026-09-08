package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThunderingBroodwagonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an opponent's nonland permanent with mana value 4 or less")
    void etbDestroysEligibleOpponentPermanent() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new ThunderingBroodwagon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thundering Broodwagon");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB cannot target a land or a permanent controlled by its caster")
    void etbRejectsLandAndOwnPermanent() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ThunderingBroodwagon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID ownPermanentId = harness.getPermanentId(player1, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.getGameService()
                .playCard(harness.getGameData(), player1, 0, 0, ownPermanentId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        UUID landId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.getGameService()
                .playCard(harness.getGameData(), player1, 0, 0, landId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("Crew 3 animates Thundering Broodwagon and taps the crew")
    void crewAnimatesBroodwagonAndTapsCrew() {
        Permanent broodwagon = harness.addToBattlefieldAndReturn(player1, new ThunderingBroodwagon());
        broodwagon.setSummoningSick(false);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        crew.setSummoningSick(false);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(broodwagon), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, broodwagon)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards Thundering Broodwagon and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ThunderingBroodwagon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thundering Broodwagon");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
