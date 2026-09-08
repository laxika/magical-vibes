package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreatDesertProspectorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one tapped Powerstone for each other creature you control")
    void createsTappedPowerstonesForOtherCreatures() {
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.addToBattlefield(player2, new YouthfulKnight());

        castProspector();

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Powerstone"))
                .toList();
        assertThat(powerstones).hasSize(2);
        assertThat(powerstones).allSatisfy(powerstone -> {
            assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
            assertThat(powerstone.isTapped()).isTrue();
        });
    }

    @Test
    @DisplayName("Powerstone mana is tracked as mana that cannot pay a nonartifact spell")
    void powerstoneManaUsesPowerstoneRestriction() {
        harness.addToBattlefield(player1, new YouthfulKnight());
        castProspector();

        Permanent powerstone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Powerstone"))
                .findFirst()
                .orElseThrow();
        powerstone.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(powerstone), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    private void castProspector() {
        harness.setHand(player1, List.of(new GreatDesertProspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
