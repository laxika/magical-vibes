package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraFracture.class, AngelicChorus.class, Forest.class, GrizzlyBears.class})
class AuraFractureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land destroys target enchantment")
    void sacrificingLandDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Aura Fracture cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aura Fracture requires a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice a creature instead of a land")
    void cannotSacrificeCreatureInsteadOfLand() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Can choose which land to sacrifice when multiple lands are available")
    void choosesLandToSacrifice() {
        harness.addToBattlefield(player1, new AuraFracture());
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(firstLand.getId()));
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondLand.getId()));
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Can target Aura Fracture itself")
    void canTargetItself() {
        var auraFracture = harness.addToBattlefieldAndReturn(player1, new AuraFracture());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, auraFracture.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aura Fracture");
        harness.assertInGraveyard(player1, "Aura Fracture");
        harness.assertInGraveyard(player1, "Forest");
    }
    @Test
    @DisplayName("A tapped land is sacrificed immediately as a cost, before destruction resolves")
    void tappedLandIsSacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new AuraFracture());
        var land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setTapped(true);
        var target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Angelic Chorus");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new Forest());
        var target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Aura Fracture can activate repeatedly without tapping or paying mana")
    void canActivateRepeatedly() {
        var source = harness.addToBattlefieldAndReturn(player1, new AuraFracture());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        var firstTarget = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        var secondTarget = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        harness.activateAbility(player1, 0, 0, null, firstTarget.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Forest"));
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Angelic Chorus")).isEqualTo(1);
        assertThat(source.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, secondTarget.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Aura Fracture");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(source.isTapped()).isFalse();
    }
}
