package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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

@CardUsed({ThunderingBroodwagon.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, LeoninScimitar.class, AirElemental.class})
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
        harness.castArtifact(player1, 0, targetId);

        resolveAllTriggers();

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
        assertThatThrownBy(() -> harness.castArtifact(player1, 0, ownPermanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        UUID landId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castArtifact(player1, 0, landId))
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

    @Test
    void etbDestroysManaValueFourCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ThunderingBroodwagon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void canEnterWhenNoEligibleTargetExists() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new ThunderingBroodwagon(), "{2}{B}{B}{G}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Thundering Broodwagon");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent broodwagon = harness.addToBattlefieldAndReturn(player1, new ThunderingBroodwagon());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, broodwagon)).isTrue();
    }

    @Test
    void insufficientPowerCannotCrew() {
        Permanent broodwagon = harness.addToBattlefieldAndReturn(player1, new ThunderingBroodwagon());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, broodwagon)).isFalse();
    }
}
