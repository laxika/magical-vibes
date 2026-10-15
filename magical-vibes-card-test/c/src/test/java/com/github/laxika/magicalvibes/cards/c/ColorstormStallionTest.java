package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColorstormStallion.class, Shock.class, Hurricane.class, GrizzlyBears.class})
class ColorstormStallionTest extends BaseCardTest {

    private Permanent addStallion(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ColorstormStallion());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private long countStallionTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Colorstorm Stallion") && p.getCard().isToken())
                .count();
    }

    @Test
    @DisplayName("Casting a one-mana instant gives +1/+1 and no token")
    void castingCheapInstantBoostsWithoutToken() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(stallion.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Casting a spell with four mana spent gives +1/+1 but no token")
    void castingFourManaSpellBoostsWithoutToken() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(stallion.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Casting a spell with five or more mana spent gives +1/+1 and a token copy")
    void castingFiveManaSpellCreatesTokenCopy() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(stallion.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isEqualTo(1);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Colorstorm Stallion") && p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getPowerModifier()).isZero();
        assertThat(token.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(2);
        assertThat(token.getPowerModifier()).isEqualTo(1);
        assertThat(token.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple instant casts stack the boost and create tokens when mana threshold is met")
    void multipleCastsStackBoostAndTokens() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isZero();

        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(2);
        assertThat(stallion.getToughnessModifier()).isEqualTo(2);
        assertThat(countStallionTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Colorstorm Stallion")
    void castingCreatureDoesNotTrigger() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(0);
        assertThat(stallion.getToughnessModifier()).isEqualTo(0);
        assertThat(countStallionTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Token copies retain both colors of the source")
    void tokenCopyRetainsBothColors() {
        addStallion(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.hasColor(gd, token, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, token, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger the Stallion")
    void opponentsInstantDoesNotTrigger() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isZero();
        assertThat(stallion.getToughnessModifier()).isZero();
        assertThat(countStallionTokens(player1)).isZero();
    }

    @Test
    @DisplayName("The boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(stallion.getPowerModifier()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(stallion.getPowerModifier()).isZero();
        assertThat(stallion.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when no mana remains to pay")
    void wardCountersSpellWhenPaymentIsUnavailable() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.castInstant(player2, 0, stallion.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(stallion.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Token copies can create further copies on later expensive casts")
    void tokensCreateFurtherCopies() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countStallionTokens(player1)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(2);
        assertThat(countStallionTokens(player1)).isEqualTo(3);
    }
}
