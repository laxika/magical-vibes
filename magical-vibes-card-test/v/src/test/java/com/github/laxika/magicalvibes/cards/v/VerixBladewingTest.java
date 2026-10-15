package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerixBladewing.class})
class VerixBladewingTest extends BaseCardTest {

    @Test
    @DisplayName("Kicker requires three additional mana")
    void kickerRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Verix Bladewing");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker — enters as 4/4, no token created")
    void castWithoutKickerNoToken() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Verix Bladewing");
        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();
        // Only Verix on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 7); // {2}{R}{R} + {3} kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Verix Bladewing");
        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — creates Karox Bladewing legendary 4/4 red Dragon token with flying")
    void castWithKickerCreatesKaroxToken() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Verix + Karox = 2 permanents
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        // Verify the Karox Bladewing token
        Permanent karox = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Karox Bladewing"));

        assertThat(karox.getCard().getPower()).isEqualTo(4);
        assertThat(karox.getCard().getToughness()).isEqualTo(4);
        assertThat(karox.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(karox.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(karox.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(karox.getCard().isToken()).isTrue();
        assertThat(karox.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Entering without being cast does not create Karox")
    void enteringWithoutCastingDoesNotCreateToken() {
        harness.enterBattlefieldAndReturn(player1, new VerixBladewing());

        harness.assertOnBattlefield(player1, "Verix Bladewing");
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Karox Bladewing");
    }

    @Test
    @DisplayName("Karox is created even if Verix leaves before its trigger resolves")
    void triggerResolvesAfterVerixLeaves() {
        harness.setHand(player1, List.of(new VerixBladewing()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent verix = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Verix Bladewing"));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, verix));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Verix Bladewing");
        harness.assertOnBattlefield(player1, "Karox Bladewing");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
