package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfMigration.class, RayOfCommand.class})
class SoulOfMigrationTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: creates two Bird tokens and Soul of Migration stays on the battlefield")
    void hardcastCreatesBirdsAndStays() {
        harness.setHand(player1, List.of(new SoulOfMigration()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        harness.assertOnBattlefield(player1, "Soul of Migration");
    }

    @Test
    @DisplayName("Evoke: creates two Bird tokens, then sacrifices Soul of Migration")
    void evokeCreatesBirdsAndSacrificesSelf() {
        harness.setHand(player1, List.of(new SoulOfMigration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Soul of Migration - sacrifice this creature");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Soul of Migration");
        harness.assertInGraveyard(player1, "Soul of Migration");
    }

    @Test
    @DisplayName("The created Birds are 1/1 white Bird creatures with flying")
    void createsBirdsWithCorrectCharacteristics() {
        harness.castFromHand(player1, new SoulOfMigration(), "{5}{W}{W}");
        resolveAllTriggers();

        assertBirdTokens();
        assertThat(findPermanents(player2, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("Bird creation resolves even after the evoke sacrifice")
    void createsBirdsAfterSacrifice() {
        harness.setHand(player1, List.of(new SoulOfMigration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2: Soul of Migration's ETB ability");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soul of Migration");
        harness.assertNotOnBattlefield(player1, "Soul of Migration");
        assertThat(findPermanents(player1, "Bird")).isEmpty();

        resolveAllTriggers();

        assertBirdTokens();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The current controller sacrifices an evoked Soul of Migration")
    void sacrificesAfterOpponentGainsControl() {
        harness.setHand(player1, List.of(new SoulOfMigration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Soul of Migration - sacrifice this creature");
        harness.passBothPriorities();
        assertBirdTokens();

        var soul = findPermanent(player1, "Soul of Migration");
        harness.castAndResolveInstant(player2, 0, soul.getId());
        harness.assertOnBattlefield(player2, "Soul of Migration");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Soul of Migration");
        harness.assertNotOnBattlefield(player2, "Soul of Migration");
        harness.assertInGraveyard(player1, "Soul of Migration");
        assertBirdTokens();
        assertThat(findPermanents(player2, "Bird")).isEmpty();
    }

    private void assertBirdTokens() {
        assertThat(findPermanents(player1, "Bird")).hasSize(2).allSatisfy(bird -> {
            assertThat(bird.getEffectivePower()).isEqualTo(1);
            assertThat(bird.getEffectiveToughness()).isEqualTo(1);
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }
}
