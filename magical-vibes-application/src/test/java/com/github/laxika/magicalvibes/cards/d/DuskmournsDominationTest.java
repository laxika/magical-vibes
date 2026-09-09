package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskmournsDomination.class, LlanowarElves.class, FountainOfYouth.class})
class DuskmournsDominationTest extends BaseCardTest {

    @Test
    @DisplayName("Controls the enchanted creature, gives it -3/-0, and removes its abilities")
    void controlsWeakensAndRemovesAbilities() {
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        castDomination(elves);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(elves.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(elves.getId()));
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.hasLostAllAbilities(gd, elves)).isTrue();

        int elvesIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elves);
        assertThatThrownBy(() -> harness.tapPermanent(player1, elvesIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lost its abilities");
    }

    @Test
    @DisplayName("Can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DuskmournsDomination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castDomination(Permanent target) {
        harness.setHand(player1, List.of(new DuskmournsDomination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
