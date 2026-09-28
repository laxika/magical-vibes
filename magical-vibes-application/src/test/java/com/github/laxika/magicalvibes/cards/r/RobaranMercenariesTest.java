package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobaranMercenaries.class, CaptainSisay.class, ProdigalPyromancer.class})
class RobaranMercenariesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from legendary creatures you control")
    void gainsLegendaryCreatureAbilities() {
        Permanent robaran = addReady(player1, new RobaranMercenaries());
        addReady(player1, new CaptainSisay());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(robaran.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not gain activated abilities from nonlegendary creatures")
    void ignoresNonlegendaryCreatures() {
        addReady(player1, new RobaranMercenaries());
        addReady(player1, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain activated abilities from legendary creatures an opponent controls")
    void ignoresOpponentsLegendaryCreatures() {
        addReady(player1, new RobaranMercenaries());
        addReady(player2, new CaptainSisay());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
