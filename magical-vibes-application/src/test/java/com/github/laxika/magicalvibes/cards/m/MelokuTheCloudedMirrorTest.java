package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MelokuTheCloudedMirror.class, Island.class, Plains.class})
class MelokuTheCloudedMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and creates a 1/1 flying Illusion token")
    void returnsLandAndCreatesToken() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        harness.activateAbility(player1, melokuIndex, null, null);

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Illusion");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ILLUSION);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        assertThatThrownBy(() -> harness.activateAbility(player1, melokuIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addToBattlefield(player1, new Island());

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        assertThatThrownBy(() -> harness.activateAbility(player1, melokuIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot return an opponent's land as the cost")
    void cannotReturnOpponentsLand() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        assertThatThrownBy(() -> harness.activateAbility(player1, melokuIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesLandWhenSeveralAvailable() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addToBattlefield(player1, new Island());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        harness.activateAbility(player1, melokuIndex, null, null);

        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, plains.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Can activate a tapped Meloku and return a tapped land")
    void canActivateWhileTappedAndReturnTappedLand() {
        Permanent meloku = harness.addToBattlefieldAndReturn(player1, new MelokuTheCloudedMirror());
        meloku.setTapped(true);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInHand(player1, "Island");
        assertThat(countPermanents(player1, "Illusion")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Illusion")).isEqualTo(1);
        assertThat(meloku.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A controlled land returns to its owner's hand")
    void returnsControlledLandToOpponentsHand() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        Island island = new Island();
        island.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, island);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Illusion")).isEqualTo(1);
        assertThat(countPermanents(player2, "Illusion")).isZero();
    }

    @Test
    @DisplayName("Can activate repeatedly before either ability resolves")
    void canActivateRepeatedlyBeforeResolution() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, island.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Plains");
        assertThat(gd.stack).hasSize(2);
        assertThat(countPermanents(player1, "Illusion")).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Illusion")).isEqualTo(2);
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
