package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LegionsChant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossalChorus.class, ColossalDreadmaw.class, LegionsChant.class})
class ColossalChorusTest extends BaseCardTest {

    @Test
    void conjuresDreadmawsEqualToStartingIntensity() {
        ColossalChorus chorus = new ColossalChorus();
        harness.setHand(player1, List.of(chorus));
        addColossalChorusMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(permanent -> permanent.getCard() instanceof ColossalDreadmaw);
        assertThat(gd.getCardIntensity(chorus.getId())).isEqualTo(3);
    }

    @Test
    void intensifiesAllOwnedChorusCards() {
        ColossalChorus chorus = new ColossalChorus();
        LegionsChant otherChorus = new LegionsChant();
        harness.setHand(player1, List.of(chorus));
        harness.setLibrary(player1, List.of(otherChorus));
        addColossalChorusMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(chorus.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(otherChorus.getId())).isEqualTo(1);
    }

    private void addColossalChorusMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
