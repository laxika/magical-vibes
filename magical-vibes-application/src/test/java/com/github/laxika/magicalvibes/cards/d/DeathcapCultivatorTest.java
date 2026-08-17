package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setGraveyard(player1, List.of(new Plains(), new Shock(), new LeoninScimitar()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void hasDeathtouchWithFourCardTypesInControllerGraveyard() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void opponentGraveyardDoesNotCountForDelirium() {
        Permanent cultivator = addCreatureReady(player1, new DeathcapCultivator());
        harness.setGraveyard(player2, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.hasKeyword(gd, cultivator, Keyword.DEATHTOUCH)).isFalse();
    }
}
