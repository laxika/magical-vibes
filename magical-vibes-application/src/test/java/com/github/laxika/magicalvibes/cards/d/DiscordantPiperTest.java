package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscordantPiper.class})
class DiscordantPiperTest extends BaseCardTest {

    @Test
    @DisplayName("When Discordant Piper dies, it creates a 0/1 white Goat token")
    void createsGoatWhenItDies() {
        Permanent piper = harness.addToBattlefieldAndReturn(player1, new DiscordantPiper());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, piper));
        harness.passBothPriorities();

        Permanent goat = findPermanent(player1, "Goat");
        assertThat(goat.getCard().isToken()).isTrue();
        assertThat(goat.getCard().getPower()).isZero();
        assertThat(goat.getCard().getToughness()).isEqualTo(1);
        assertThat(goat.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(goat.getCard().getSubtypes()).contains(CardSubtype.GOAT);
    }

    @Test
    @DisplayName("Discordant Piper's death trigger creates the Goat for its controller")
    void deathTriggerBelongsToController() {
        Permanent piper = harness.addToBattlefieldAndReturn(player2, new DiscordantPiper());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, piper));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Goat")).hasSize(1);
        assertThat(findPermanents(player1, "Goat")).isEmpty();
    }
}
