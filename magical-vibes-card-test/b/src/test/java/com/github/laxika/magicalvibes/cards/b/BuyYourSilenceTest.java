package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RaffinesInformant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuyYourSilence.class, GrizzlyBears.class, Plains.class, RaffinesInformant.class})
class BuyYourSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent and its controller creates a Treasure")
    void exilesNonlandPermanentAndCreatesTreasureForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Fizzles without creating a Treasure if the target leaves before resolution")
    void fizzlesWithoutCreatingTreasureWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Can exile your own permanent and gives you the Treasure")
    void exilesOwnPermanentAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RaffinesInformant());
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Raffine's Informant");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure").getFirst().isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Exiling a Treasure token gives its controller a new Treasure")
    void exilesNoncreatureTokenAndCreatesReplacementTreasure() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaffinesInformant());
        harness.setHand(player1, List.of(new BuyYourSilence(), new BuyYourSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        Permanent treasure = findPermanents(player2, "Treasure").getFirst();

        harness.castAndResolveSorcery(player1, 0, treasure.getId());

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure").getFirst().getId()).isNotEqualTo(treasure.getId());
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Treasure goes to the permanent's controller at resolution")
    void createsTreasureForControllerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaffinesInformant());
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}
