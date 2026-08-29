package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchitectOfTheUntamedTest extends BaseCardTest {

    @Test
    void landfallGivesOneEnergy() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEightEnergyToCreateSixSixColorlessBeastArtifactCreatureToken() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent beast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Beast"))
                .findFirst()
                .orElseThrow();
        assertThat(beast.getEffectivePower()).isEqualTo(6);
        assertThat(beast.getEffectiveToughness()).isEqualTo(6);
        assertThat(beast.getCard().getColor()).isNull();
        assertThat(beast.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    void cannotActivateWithoutEightEnergy() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");
    }
}
