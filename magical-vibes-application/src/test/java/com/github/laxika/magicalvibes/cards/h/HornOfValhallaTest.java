package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YsgardsCall;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornOfValhalla.class, YsgardsCall.class, GrizzlyBears.class})
class HornOfValhallaTest extends BaseCardTest {

    @Test
    @DisplayName("Adventure creates X white Soldier tokens")
    void adventureCreatesSoldierTokens() {
        HornOfValhalla card = new HornOfValhalla();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 3, Map.of());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Soldier");
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each creature its controller controls")
    void equippedCreatureScalesWithControlledCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfValhalla());
        horn.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Equipment face can be cast from exile after Adventure")
    void equipmentFaceCanBeCastFromExileAfterAdventure() {
        HornOfValhalla card = new HornOfValhalla();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Horn of Valhalla"));
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
