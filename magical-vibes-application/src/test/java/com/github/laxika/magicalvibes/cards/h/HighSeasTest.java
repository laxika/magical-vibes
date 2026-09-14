package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.r.RockBadger;
import com.github.laxika.magicalvibes.cards.r.RushwoodDryad;
import com.github.laxika.magicalvibes.cards.s.Sizzle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighSeas.class, RockBadger.class, RushwoodDryad.class, CloudSprite.class, Sizzle.class})
class HighSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature spells cost {1} more")
    void redCreatureSpellsCostMore() {
        harness.addToBattlefield(player1, new HighSeas());

        assertThatThrownBy(() -> harness.castFromHand(player1, new RockBadger(), "{4}{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Red creature spells are castable with the additional generic mana")
    void redCreatureSpellsAreCastableWithAdditionalMana() {
        harness.addToBattlefield(player1, new HighSeas());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new RockBadger(), "{4}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Green creature spells cost {1} more")
    void greenCreatureSpellsCostMore() {
        harness.addToBattlefield(player1, new HighSeas());

        assertThatThrownBy(() -> harness.castFromHand(player1, new RushwoodDryad(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Green creature spells are castable with the additional generic mana")
    void greenCreatureSpellsAreCastableWithAdditionalMana() {
        harness.addToBattlefield(player1, new HighSeas());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new RushwoodDryad(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Non-red and non-green creature spells are not affected")
    void otherColoredCreatureSpellsAreNotAffected() {
        harness.addToBattlefield(player1, new HighSeas());

        harness.castFromHand(player1, new CloudSprite(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Red noncreature spells are not affected")
    void redNoncreatureSpellsAreNotAffected() {
        harness.addToBattlefield(player1, new HighSeas());

        harness.castFromHand(player1, new Sizzle(), "{2}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The cost increase applies to opponents")
    void costIncreaseAppliesToOpponents() {
        harness.addToBattlefield(player1, new HighSeas());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new RockBadger(), "{4}{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple High Seas effects increase matching costs cumulatively")
    void multipleHighSeasEffectsStack() {
        harness.addToBattlefield(player1, new HighSeas());
        harness.addToBattlefield(player1, new HighSeas());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new RockBadger(), "{4}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
