package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrSire.class, WrathOfGod.class, DivineOffering.class})
class MyrSireTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Myr Sire puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MyrSire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Myr Sire");
    }


    @Test
    @DisplayName("When Myr Sire dies, a Phyrexian Myr token is created")
    void deathTriggerCreatesToken() {
        harness.addToBattlefield(player1, new MyrSire());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();

        // Myr Sire should be in the graveyard
        harness.assertInGraveyard(player1, "Myr Sire");

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Phyrexian Myr token should be on the battlefield
        List<Permanent> tokens = findPermanents(player1, "Phyrexian Myr");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Death trigger token is a 1/1 colorless Phyrexian Myr artifact creature")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new MyrSire());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve death trigger

        Permanent token = findPermanent(player1, "Phyrexian Myr");

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.PHYREXIAN, CardSubtype.MYR);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Myr Sire gives its controller the token")
    void opponentsSireCreatesTokenForOpponent() {
        harness.addToBattlefield(player2, new MyrSire());
        Permanent sire = findPermanent(player2, "Myr Sire");
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, sire.getId());

        harness.assertInGraveyard(player2, "Myr Sire");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player2, "Phyrexian Myr")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Myr")).hasSize(1);
        assertThat(findPermanents(player1, "Phyrexian Myr")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created token does not inherit Myr Sire's death ability")
    void tokenDyingDoesNotCreateAnotherToken() {
        harness.addToBattlefield(player1, new MyrSire());
        harness.setHand(player1, List.of(new DivineOffering(), new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Myr Sire").getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Phyrexian Myr")).hasSize(1);

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Phyrexian Myr").getId());

        assertThat(findPermanents(player1, "Phyrexian Myr")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}

