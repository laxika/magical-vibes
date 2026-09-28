package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnduSpiritdancer.class, GloriousAnthem.class})
class OnduSpiritdancerTest extends BaseCardTest {

    @Test
    @DisplayName("May create a token copy when an enchantment enters under its control")
    void createsTokenCopyWhenAccepted() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();

        resolveAnthemAndTrigger();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(2);
        assertThat(findPermanents(player1, "Glorious Anthem")).anyMatch(permanent ->
                permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not create a token copy when the may ability is declined")
    void decliningDoesNotCreateTokenCopy() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();

        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        harness.setHand(player1, List.of(new GloriousAnthem(), new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0);
        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(3);
    }

    private void castAnthem() {
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
    }

    private void resolveAnthemAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
