package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpicDownfall.class, Forest.class, GrayOgre.class, GrizzlyBears.class})
class EpicDownfallTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature with mana value 3 or greater")
    void exilesCreatureAtManaValueBoundary() {
        harness.addToBattlefield(player2, new GrayOgre());
        UUID targetId = harness.getPermanentId(player2, "Gray Ogre");
        castEpicDownfall(targetId);

        harness.assertNotOnBattlefield(player2, "Gray Ogre");
        harness.assertNotInGraveyard(player2, "Gray Ogre");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Gray Ogre"));
    }

    @Test
    @DisplayName("Cannot target a creature with mana value less than 3")
    void cannotTargetLowManaValueCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareCardAndMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or greater");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        prepareCardAndMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castEpicDownfall(UUID targetId) {
        prepareCardAndMana();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareCardAndMana() {
        harness.setHand(player1, List.of(new EpicDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
