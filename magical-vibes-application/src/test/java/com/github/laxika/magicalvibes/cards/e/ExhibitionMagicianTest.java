package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExhibitionMagician.class})
class ExhibitionMagicianTest extends BaseCardTest {

    @Test
    void createsCitizenToken() {
        castExhibitionMagician(0);

        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        Permanent citizen = findPermanents(player1, "Citizen").getFirst();
        assertThat(citizen.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(citizen.getCard().getPower()).isEqualTo(1);
        assertThat(citizen.getCard().getToughness()).isEqualTo(1);
        assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(citizen.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
    }

    @Test
    void createsTreasureToken() {
        castExhibitionMagician(1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        assertThat(treasure.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(treasure.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
        assertThat(treasure.isTapped()).isFalse();
        assertThat(treasure.getCard().getColors()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        castExhibitionMagician(1);
        Permanent treasure = findPermanent(player1, "Treasure");
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(color);
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(manaBefore + 1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutCastingAllowsChoosingTreasure() {
        harness.enterBattlefieldAndReturn(player1, new ExhibitionMagician());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Create a Treasure token");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
    }

    private void castExhibitionMagician(int mode) {
        harness.setHand(player1, List.of(new ExhibitionMagician()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, mode);
        resolveAllTriggers();
    }
}
