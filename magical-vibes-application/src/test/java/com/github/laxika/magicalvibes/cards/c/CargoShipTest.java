package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CargoShip.class, GrizzlyBears.class})
class CargoShipTest extends BaseCardTest {

    @Test
    void addsArtifactOnlyColorlessMana() {
        Permanent ship = addShipReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(1);
        assertThat(ship.isTapped()).isTrue();
    }

    @Test
    void artifactOnlyManaCanCastArtifactSpell() {
        addShipReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        harness.setHand(player1, List.of(artifact));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    void artifactOnlyManaCannotCastNonartifactSpell() {
        addShipReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        Card creature = card("Test Creature", CardType.CREATURE);
        harness.setHand(player1, List.of(creature));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void crewAnimatesShipAndTapsCrew() {
        Permanent ship = addShipReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ship.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addShipReady(Player player) {
        Permanent ship = new Permanent(new CargoShip());
        ship.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ship);
        return ship;
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        return card;
    }
}
