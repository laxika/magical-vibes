package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.cards.e.EbonyTreefolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathMutation.class, Dodecapod.class, EbonyTreefolk.class, BattlefieldForge.class})
class DeathMutationTest extends BaseCardTest {

    @Test
    void destroysNonblackCreatureAndCreatesSaprolingsEqualToItsManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        cast(target);

        harness.assertNotOnBattlefield(player2, "Dodecapod");
        harness.assertInGraveyard(player2, "Dodecapod");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(4);
    }

    @Test
    void createsGreenOneOneSaprolingCreatureTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        cast(target);

        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(4)
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
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        target.setRegenerationShield(1);
        cast(target);

        harness.assertNotOnBattlefield(player2, "Dodecapod");
        harness.assertInGraveyard(player2, "Dodecapod");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(4);
    }

    @Test
    void createsSaprolingsEvenWhenIndestructibleTargetSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        cast(target);

        harness.assertOnBattlefield(player2, "Dodecapod");
        harness.assertNotInGraveyard(player2, "Dodecapod");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(4);
    }

    @Test
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EbonyTreefolk());
        harness.setHand(player1, List.of(new DeathMutation()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BattlefieldForge());
        harness.setHand(player1, List.of(new DeathMutation()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new DeathMutation()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
