package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.o.OranRiefTheVastwood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpreadingSeas.class, Forest.class, KrakenHatchling.class,
        IntoTheRoil.class, OranRiefTheVastwood.class})
class SpreadingSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Spreading Seas draws a card and makes the enchanted land an Island")
    void drawsCardAndChangesEnchantedLandType() {
        Card libraryCard = new KrakenHatchling();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SpreadingSeas()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(libraryCard.getId()));

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Spreading Seas cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KrakenHatchling());
        harness.setHand(player1, List.of(new SpreadingSeas()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    void replacesNonbasicLandsPrintedActivatedAbilityWithBlueManaAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new OranRiefTheVastwood());
        harness.setLibrary(player1, List.of(new KrakenHatchling()));
        harness.setHand(player1, List.of(new SpreadingSeas()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bouncingAuraRestoresOpponentsLandAndDoesNotCounterPendingDraw() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card libraryCard = new KrakenHatchling();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SpreadingSeas(), new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(libraryCard.getId()));
        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spreading Seas");
        harness.assertInHand(player1, "Spreading Seas");
        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.FOREST);
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .anyMatch(card -> card.getId().equals(libraryCard.getId()));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
