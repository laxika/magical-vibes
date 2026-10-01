package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonbloodTwins.class, LightningBolt.class})
class DragonbloodTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell makes Dragonblood Twins 4/4 and grants flying")
    void secondSpellMakesItFourFourAndGrantsFlying() {
        Permanent twins = addCreatureReady(player1, new DragonbloodTwins());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(twins.getGrantedKeywords()).doesNotContain(Keyword.FLYING);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(twins.getEffectivePower()).isEqualTo(4);
        assertThat(twins.getEffectiveToughness()).isEqualTo(4);
        assertThat(twins.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(twins.getEffectivePower()).isEqualTo(4);
        assertThat(twins.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The flurry bonus wears off at end of turn")
    void flurryBonusWearsOffAtEndOfTurn() {
        Permanent twins = addCreatureReady(player1, new DragonbloodTwins());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(twins.getEffectivePower()).isEqualTo(4);
        assertThat(twins.getEffectiveToughness()).isEqualTo(4);
        assertThat(twins.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(twins.getEffectivePower()).isEqualTo(2);
        assertThat(twins.getEffectiveToughness()).isEqualTo(2);
        assertThat(twins.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
