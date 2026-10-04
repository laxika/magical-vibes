package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarVisionary;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinWizardry.class, Shock.class, LlanowarVisionary.class})
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

        harness.setHand(player1, List.of(new LlanowarVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        for (Permanent token : tokens) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Tokens do not trigger for the spell that created them")
    void creatingSpellDoesNotTriggerProwess() {
        harness.setHand(player1, List.of(new GoblinWizardry()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        for (Permanent token : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Opponent's noncreature spells do not trigger the tokens")
    void opponentsSpellDoesNotTriggerProwess() {
        harness.setHand(player1, List.of(new GoblinWizardry()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        for (Permanent token : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each token triggers once per cast before the spell resolves and boosts expire")
    void repeatedProwessTriggersStackAndExpire() {
        harness.setHand(player1, List.of(new GoblinWizardry(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        List<Permanent> tokens = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        assertThat(tokens).hasSize(2);

        for (int cast = 0; cast < 2; cast++) {
            harness.castInstant(player1, 0, player2.getId());
            assertThat(gd.stack).hasSize(3);
            assertThat(gd.stack.stream()
                    .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                    .hasSize(2);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.assertLife(player2, 20 - 2 * cast);
            for (Permanent token : tokens) {
                assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2 + cast);
                assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2 + cast);
            }
            harness.passBothPriorities();
            harness.assertLife(player2, 18 - 2 * cast);
        }

        harness.passUntil(player2, TurnStep.UPKEEP);
        for (Permanent token : tokens) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
    }
}
