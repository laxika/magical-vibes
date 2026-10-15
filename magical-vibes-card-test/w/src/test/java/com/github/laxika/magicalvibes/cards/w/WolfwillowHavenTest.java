package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfwillowHaven.class, Forest.class, Island.class, NyxbornColossus.class})
class WolfwillowHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the enchanted land adds an additional green mana")
    void enchantedLandAddsGreenMana() {
        Permanent forest = addEnchantedForest();

        harness.tapPermanent(player1, 0);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Wolfwillow Haven creates a 2/2 green Wolf token")
    void sacrificingCreatesWolfToken() {
        addEnchantedForest();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(wolf -> {
                    assertThat(wolf.getCard().getName()).isEqualTo("Wolf");
                    assertThat(wolf.getCard().getPower()).isEqualTo(2);
                    assertThat(wolf.getCard().getToughness()).isEqualTo(2);
                    assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
                });
        harness.assertInGraveyard(player1, "Wolfwillow Haven");
    }

    @Test
    @DisplayName("The token ability cannot be activated during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addEnchantedForest();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wolfwillow Haven can enchant only a land")
    void cannotEnchantNonLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.setHand(player1, List.of(new WolfwillowHaven()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The bonus mana is green even when the land produces blue")
    void enchantedIslandAddsGreenImmediately() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WolfwillowHaven());
        aura.setAttachedTo(island.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's enchanted land gives its controller the bonus mana")
    void canEnchantOpponentsLandAndGiveThemMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WolfwillowHaven()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wolfwillow Haven").getAttachedTo()).isEqualTo(forest.getId());
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping another land does not produce bonus mana")
    void otherLandDoesNotAddBonusMana() {
        addEnchantedForest();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The token ability can be activated in your upkeep and sacrifices the Aura as a cost")
    void canActivateDuringUpkeepAndSacrificeBeforeResolution() {
        Permanent forest = addEnchantedForest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Wolfwillow Haven");
        harness.assertInGraveyard(player1, "Wolfwillow Haven");
        assertThat(countPermanents(player1, "Wolf")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WolfwillowHaven());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
