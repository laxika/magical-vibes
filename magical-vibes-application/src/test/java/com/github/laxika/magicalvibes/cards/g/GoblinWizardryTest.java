package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinWizardryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 red Goblin Wizard tokens with prowess")
    void createsTwoGoblinWizardTokensWithProwess() {
        harness.setHand(player1, List.of(new GoblinWizardry()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.WIZARD);
        }
    }

    @Test
    @DisplayName("The created tokens get +1/+1 when their controller casts a noncreature spell")
    void createdTokensHaveProwess() {
        harness.setHand(player1, List.of(new GoblinWizardry(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (Permanent token : tokens) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("The created tokens do not get prowess from a creature spell")
    void creatureSpellDoesNotTriggerProwess() {
        harness.setHand(player1, List.of(new GoblinWizardry()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        for (Permanent token : tokens) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
    }
}
