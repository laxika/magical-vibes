package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.cards.r.RimefeatherOwl;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Icequake.class, Forest.class, SnowCoveredForest.class, BalduvianBears.class,
        GlacialChasm.class, RimefeatherOwl.class})
class IcequakeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonsnow land without dealing damage")
    void destroysNonsnowLandNoDamage() {
        castIcequake(harness.addToBattlefieldAndReturn(player2, new Forest()).getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroys snow land and deals 1 damage to its controller")
    void destroysSnowLandAndDealsDamage() {
        Permanent snow = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        castIcequake(snow.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(snow);
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not deal damage if the target is no longer snow when Icequake resolves")
    void doesNotDealDamageIfTargetIsNoLongerSnowAtResolution() {
        Permanent snow = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Icequake()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, snow.getId());

        TestCards.mutableCard(snow).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Forest");
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BalduvianBears()).getId();
        harness.setHand(player1, List.of(new Icequake()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Destroying a snow Glacial Chasm removes prevention before dealing damage")
    void destroysSnowChasmBeforeDealingDamage() {
        harness.addToBattlefield(player1, new RimefeatherOwl());
        Permanent chasm = harness.addToBattlefieldAndReturn(player2, new GlacialChasm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, chasm.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSupertype(gd, chasm, CardSupertype.SNOW)).isTrue();

        castIcequake(chasm.getId());

        harness.assertNotOnBattlefield(player2, "Glacial Chasm");
        harness.assertInGraveyard(player2, "Glacial Chasm");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can destroy its controller's snow land and damage that controller")
    void damagesControllerOfOwnSnowLand() {
        Permanent snow = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());

        castIcequake(snow.getId());

        harness.assertNotOnBattlefield(player1, "Snow-Covered Forest");
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not deal damage when the snow land target has left the battlefield")
    void doesNotDealDamageToControllerOfMissingTarget() {
        Permanent snow = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Icequake()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, snow.getId());
        gd.playerBattlefields.get(player2.getId()).remove(snow);
        gd.playerGraveyards.get(player2.getId()).add(snow.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Icequake");
        assertThat(gd.stack).isEmpty();
    }

    private void castIcequake(UUID targetId) {
        harness.setHand(player1, List.of(new Icequake()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
