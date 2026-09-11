package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DesolationTwin.class)
class DesolationTwinTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Desolation Twin creates a 10/10 colorless Eldrazi token")
    void castTriggerCreatesEldraziTokenBeforeSpellResolves() {
        harness.setHand(player1, List.of(new DesolationTwin()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Eldrazi");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELDRAZI);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(10);
        harness.assertNotOnBattlefield(player1, "Desolation Twin");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Desolation Twin");
    }
}
