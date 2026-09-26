package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({VisionsOfGlory.class, GrizzlyBears.class, EdgarMarkov.class})
class VisionsOfGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Human token for each creature you control")
    void createsHumansForEachControlledCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VisionsOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertHumans(2);
    }

    @Test
    @DisplayName("Flashback cost is reduced by the greatest owned commander in the command zone")
    void flashbackUsesCommanderInCommandZone() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        VisionsOfGlory spell = new VisionsOfGlory();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertHumans(0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback cost is reduced by the greatest owned commander on the battlefield")
    void flashbackUsesCommanderOnBattlefield() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        VisionsOfGlory spell = new VisionsOfGlory();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertHumans(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback still requires its full cost without a commander")
    void flashbackRequiresFullCostWithoutCommander() {
        VisionsOfGlory spell = new VisionsOfGlory();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    private void assertHumans(int expectedCount) {
        List<Permanent> humans = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Human"))
                .toList();
        assertThat(humans).hasSize(expectedCount);
        assertThat(humans).allSatisfy(human -> {
            assertThat(human.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(human.getCard().getPower()).isEqualTo(1);
            assertThat(human.getCard().getToughness()).isEqualTo(1);
            assertThat(human.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(human.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
        });
    }
}
