package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ExplosiveApparatus;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathcapCultivator.class, Plains.class, DualShot.class, ExplosiveApparatus.class, DeadWeight.class})
class DeathcapCultivatorTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {B}")
    void tapsForBlackMana() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(cultivator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}: Add {G}")
    void tapsForGreenMana() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(cultivator.isTapped()).isTrue();
    }

    @Test
    void doesNotHaveDeathtouchWithoutDelirium() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player1, List.of(new Plains(), new DualShot(), new ExplosiveApparatus()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void hasDeathtouchWithFourCardTypesInControllerGraveyard() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player1, List.of(
                new Plains(), new DualShot(), new ExplosiveApparatus(), new DeadWeight()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void opponentGraveyardDoesNotCountForDelirium() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player2, List.of(
                new Plains(), new DualShot(), new ExplosiveApparatus(), new DeadWeight()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void deathtouchUpdatesWhenGraveyardTypesChange() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player1, List.of(new Plains(), new DualShot(), new ExplosiveApparatus()));
        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();

        harness.setGraveyard(player1, List.of(
                new Plains(), new DualShot(), new ExplosiveApparatus(), new DeadWeight()));
        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isTrue();

        harness.setGraveyard(player1, List.of(new Plains(), new DualShot(), new ExplosiveApparatus()));
        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void repeatedCardTypesDoNotEnableDelirium() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new DualShot(), new ExplosiveApparatus()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void creatureTypeCountsAndDeathtouchAppliesOnlyToCultivator() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new ExplosiveApparatus());
        harness.setGraveyard(player1, List.of(
                new Plains(), new DualShot(), new ExplosiveApparatus(), new DeathcapCultivator()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, apparatus, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotActivateEitherManaAbilityWithSummoningSickness() {
        harness.addToBattlefield(player1, new DeathcapCultivator());

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("summoning sickness");
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void tappingForBlackPreventsAlsoTappingForGreen() {
        addCreatureReady(player1, new DeathcapCultivator());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
