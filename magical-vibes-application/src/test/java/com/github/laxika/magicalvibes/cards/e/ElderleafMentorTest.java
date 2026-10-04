package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderleafMentor.class})
class ElderleafMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Elderleaf Mentor creates a 1/1 Elf Warrior token when it enters")
    void createsElfWarriorTokenOnEnter() {
        harness.setHand(player1, List.of(new ElderleafMentor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Elf Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering without being cast creates the token for the entering creature's controller")
    void createsTokenWithoutBeingCastForOpponent() {
        harness.enterBattlefieldAndReturn(player2, new ElderleafMentor());

        assertThat(findPermanents(player2, "Elf Warrior")).isEmpty();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player2, "Elf Warrior");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Elderleaf Mentor entering creates its own token")
    void eachEntryCreatesOneToken() {
        harness.enterBattlefieldAndReturn(player1, new ElderleafMentor());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new ElderleafMentor());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2);
        assertThat(findPermanents(player2, "Elf Warrior")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
