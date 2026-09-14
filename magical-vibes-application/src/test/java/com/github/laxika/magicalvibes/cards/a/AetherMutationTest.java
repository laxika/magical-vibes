package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherMutation.class, Dodecapod.class, GaeasSkyfolk.class, PhyrexianArena.class})
class AetherMutationTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndCreatesSaprolingsEqualToItsManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        cast(target);

        harness.assertInHand(player2, "Gaea's Skyfolk");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }

    @Test
    void createsTheTargetManaValueNumberOfTokensForLargerCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        cast(target);

        harness.assertInHand(player2, "Dodecapod");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(4);
    }

    @Test
    void createsGreenOneOneSaprolingTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        cast(target);

        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    void cannotTargetNonCreaturePermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        harness.setHand(player1, List.of(new AetherMutation()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new AetherMutation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
