package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BovineIntervention.class, GrizzlyBears.class, LeoninScimitar.class, Island.class})
class BovineInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and gives its controller a 2/2 white Ox")
    void destroysCreatureAndCreatesOxForItsController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castBovineIntervention(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertOxCreatedFor(player2);
        assertThat(findPermanents(player1, "Ox")).isEmpty();
    }

    @Test
    @DisplayName("Destroys an artifact and gives its controller a 2/2 white Ox")
    void destroysArtifactAndCreatesOxForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castBovineIntervention(target);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertOxCreatedFor(player2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareBovineIntervention();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    private void castBovineIntervention(Permanent target) {
        prepareBovineIntervention();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareBovineIntervention() {
        harness.setHand(player1, List.of(new BovineIntervention()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void assertOxCreatedFor(com.github.laxika.magicalvibes.model.Player player) {
        List<Permanent> oxen = findPermanents(player, "Ox");
        assertThat(oxen).hasSize(1);
        Permanent ox = oxen.getFirst();
        assertThat(ox.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(ox.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ox.getCard().getSubtypes()).containsExactly(CardSubtype.OX);
        assertThat(ox.getCard().getPower()).isEqualTo(2);
        assertThat(ox.getCard().getToughness()).isEqualTo(2);
    }
}
