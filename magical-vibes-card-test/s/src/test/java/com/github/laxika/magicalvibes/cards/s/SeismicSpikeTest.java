package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicSpike.class, Mountain.class, BorosRecruit.class})
class SeismicSpikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target land and adds two red mana")
    void destroysTargetLandAndAddsMana() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SeismicSpike()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target its controller's land")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SeismicSpike()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Mountain");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SeismicSpike()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID creatureId = harness.getPermanentId(player2, "Boros Recruit");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adds mana even when the target land is indestructible")
    void addsManaWhenLandCannotBeDestroyed() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new SeismicSpike()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Seismic Spike");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add mana when the target land leaves before resolution")
    void doesNotAddManaWhenTargetLeaves() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SeismicSpike()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, mountain.getId());

        gd.playerBattlefields.get(player2.getId()).remove(mountain);
        gd.playerGraveyards.get(player2.getId()).add(mountain.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Seismic Spike");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
