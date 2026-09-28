package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantOfSecrets.class, Forest.class})
class MerchantOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new MerchantOfSecrets(), "{2}{U}");
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB draw trigger

        // Cast Merchant (hand -1), then drew 1 → net 0
        harness.assertInHand(player1, "Forest");
    }
}
