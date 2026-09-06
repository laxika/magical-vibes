package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JetmirNexusOfRevels.class, GrizzlyBears.class})
class JetmirNexusOfRevelsTest extends BaseCardTest {

    @Test
    @DisplayName("Has no threshold bonuses below three creatures")
    void noBonusesBelowThreeCreatures() {
        Permanent jetmir = addJetmir();

        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("At three creatures, own creatures get +1/+0 and vigilance")
    void firstThreshold() {
        Permanent jetmir = addJetmir();
        addCreatures(player1, 2);

        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("At six creatures, the second threshold is cumulative")
    void secondThreshold() {
        Permanent jetmir = addJetmir();
        addCreatures(player1, 5);

        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("At nine creatures, all three thresholds are cumulative")
    void thirdThreshold() {
        Permanent jetmir = addJetmir();
        addCreatures(player1, 8);

        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Counts only creatures controlled by Jetmir's controller and updates dynamically")
    void countsOwnCreaturesAndUpdatesDynamically() {
        Permanent jetmir = addJetmir();
        addCreatures(player2, 8);

        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.VIGILANCE)).isFalse();

        addCreatures(player1, 8);
        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.getEffectivePower(gd, jetmir)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, jetmir, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addJetmir() {
        return harness.addToBattlefieldAndReturn(player1, new JetmirNexusOfRevels());
    }

    private void addCreatures(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GrizzlyBears());
        }
    }
}
