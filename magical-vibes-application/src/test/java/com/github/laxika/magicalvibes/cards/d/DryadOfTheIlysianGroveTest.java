package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.u.UnknownShores;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({DryadOfTheIlysianGrove.class, Forest.class, Ichthyomorphosis.class, UnknownShores.class})
class DryadOfTheIlysianGroveTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play one additional land each turn")
    void controllerGetsAdditionalLandPlay() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller's lands gain every basic land type and can produce any color")
    void controllerLandsGainBasicTypesAndAnyColorMana() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, ownForest))
                .containsExactlyInAnyOrder(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, opponentForest))
                .containsExactly(CardSubtype.FOREST);

        harness.activateAbility(player1, 1, null, null);

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(opponentForest.isTapped()).isFalse();
    }

    @Test
    void permitsTwoLandPlaysButNotThree() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void multipleDryadsAllowCumulativeLandPlays() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(3);
    }

    @Test
    void enteringAfterNormalLandPlayAllowsOneMore() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
    }

    @Test
    void leavingRemovesTypesAndReturningDoesNotResetLandPlays() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new DryadOfTheIlysianGrove());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        gd.playerBattlefields.get(player1.getId()).remove(dryad);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void doesNotPermitLandPlaysDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void losingAbilitiesRemovesAdditionalLandPermission() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new DryadOfTheIlysianGrove());
        enchantWithIchthyomorphosis(dryad);
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void landsRetainBasicTypesAndManaAbilitiesWhenDryadLosesAbilities() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new DryadOfTheIlysianGrove());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        enchantWithIchthyomorphosis(dryad);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST);

        harness.tapPermanent(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void tappingNonbasicLandForIntrinsicManaProducesOnlyOneMana() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        Permanent shores = harness.addToBattlefieldAndReturn(player1, new UnknownShores());

        harness.tapPermanent(player1, 1);

        assertThat(shores.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void nonbasicLandRetainsItsOriginalColorlessManaAbility() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        harness.addToBattlefield(player1, new UnknownShores());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private void enchantWithIchthyomorphosis(Permanent dryad) {
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, dryad.getId());
        harness.passBothPriorities();
    }
}
