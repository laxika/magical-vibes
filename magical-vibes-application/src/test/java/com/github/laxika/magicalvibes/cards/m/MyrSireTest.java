package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyrSireTest extends BaseCardTest {

    // ===== Casting =====

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

    // ===== Death trigger =====

    @Test
    @DisplayName("When Myr Sire dies, a Phyrexian Myr token is created")
    void deathTriggerCreatesToken() {
        harness.addToBattlefield(player1, new MyrSire());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — Myr Sire dies

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

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath
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
}
