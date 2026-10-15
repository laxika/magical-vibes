package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrismariPianist.class, Shock.class, Blaze.class, LavaAxe.class, GrizzlyBears.class})
class PrismariPianistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates one Elemental token")
    void instantCreatesOneElemental() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell with mana value 5 creates three Elemental tokens")
    void highManaValueSpellCreatesThreeElementals() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not create Elemental tokens")
    void creatureSpellCreatesNoElementals() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Opponent instant casts do not trigger your Pianist")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(countPermanents(player2, "Elemental")).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Created tokens have the Oracle characteristics and resolve before the spell")
    void tokensResolveBeforeTriggeringSpell() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Elemental")).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        });
        assertThat(countPermanents(player2, "Elemental")).isZero();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Pianist triggers independently")
    void multiplePianistsCreateSeparateTokens() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(6);
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    @Test
    @DisplayName("X contributes to mana value at the five-mana threshold")
    void xSpellAtThresholdCreatesThreeTokens() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(3);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A spell with mana value four still creates only one token")
    void xSpellBelowThresholdCreatesOneToken() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Removing Pianist in response does not stop its pending trigger")
    void triggerSurvivesSourceRemoval() {
        var pianist = harness.addToBattlefieldAndReturn(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, pianist.getId());
        harness.assertNotOnBattlefield(player1, "Prismari Pianist");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(3);
        assertThat(countPermanents(player2, "Elemental")).isZero();
        harness.assertLife(player2, 15);
    }
}
