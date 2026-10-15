package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeirdingWood.class, Forest.class})
class WeirdingWoodTest extends BaseCardTest {

    @Test
    @DisplayName("When Weirding Wood enters, it creates a Clue and attaches to the target land")
    void entersAndInvestigates() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WeirdingWood()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Weirding Wood")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted land gains an ability that produces two mana of one chosen color")
    void enchantedLandProducesTwoMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeirdingWood());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land retains its normal mana ability")
    void enchantedLandStillProducesNormalMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeirdingWood());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanting an opponent's land gives the Clue to the Aura controller and mana to the land controller")
    void enchantsOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WeirdingWood()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Weirding Wood").getAttachedTo()).isEqualTo(forest.getId());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Clue");

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The investigated Clue costs two mana and is sacrificed before drawing on resolution")
    void clueSacrificesToDraw() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WeirdingWood()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
