package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SolarTransformer.class)
class SolarTransformerTest extends BaseCardTest {

    @Test
    void entersTappedAndGainsThreeEnergyCounters() {
        harness.setHand(player1, List.of(new SolarTransformer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent transformer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SolarTransformer)
                .findFirst()
                .orElseThrow();
        assertThat(transformer.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent transformer = addReadyTransformer();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(transformer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void paysEnergyAndTapsForAnyColorMana() {
        Permanent transformer = addReadyTransformer();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(transformer.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTapForAnyColorWithoutEnergy() {
        addReadyTransformer();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
    }

    private Permanent addReadyTransformer() {
        Permanent transformer = harness.addToBattlefieldAndReturn(player1, new SolarTransformer());
        transformer.setSummoningSick(false);
        return transformer;
    }
}
