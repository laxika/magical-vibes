package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrbanUtopia.class, Forest.class})
class UrbanUtopiaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Urban Utopia attaches to a land and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new UrbanUtopia()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof UrbanUtopia
                        && forest.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted land gains a tap ability that produces one mana of any color")
    void enchantedLandGainsAnyColorManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UrbanUtopia());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land retains its normal mana ability")
    void enchantedLandStillProducesNormalMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UrbanUtopia());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanting an opponent's land draws for the Aura controller and grants mana to the land controller")
    void enchantingOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UrbanUtopia()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof UrbanUtopia
                        && forest.getId().equals(permanent.getAttachedTo()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land that leaves before resolution prevents the Aura from entering or drawing")
    void missingTargetPreventsEnteringAndDrawing() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new UrbanUtopia()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urban Utopia");
        harness.assertInGraveyard(player1, "Urban Utopia");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
