package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GalvanicBombardment;
import com.github.laxika.magicalvibes.cards.l.LupinePrototype;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TakeInventory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BackwoodsSurvivalists.class, Divination.class, Forest.class, GrizzlyBears.class,
        Shock.class, GalvanicBombardment.class, LupinePrototype.class, TakeInventory.class})
class BackwoodsSurvivalistsTest extends BaseCardTest {

    @Test
    @DisplayName("Remains a 4/3 without delirium")
    void noDelirium() {
        Permanent survivalists = addSurvivalists(List.of(new GrizzlyBears(), new Shock(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and trample with four card types in its controller's graveyard")
    void delirium() {
        Permanent survivalists = addSurvivalists(List.of(
                new GrizzlyBears(), new Shock(), new Divination(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard falls below four card types")
    void losesDelirium() {
        Permanent survivalists = addSurvivalists(List.of(
                new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Counts both types of an artifact creature, reaching delirium with three cards")
    void multiTypeCardEnablesDelirium() {
        Permanent survivalists = addSurvivalists(List.of(
                new LupinePrototype(), new GalvanicBombardment(), new TakeInventory()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Four cards with only three distinct card types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        Permanent survivalists = addSurvivalists(List.of(
                new LupinePrototype(), new LupinePrototype(),
                new GalvanicBombardment(), new GalvanicBombardment()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable delirium")
    void opponentGraveyardDoesNotCount() {
        Permanent survivalists = addSurvivalists(List.of(new GalvanicBombardment()));
        harness.setGraveyard(player2, List.of(
                new LupinePrototype(), new GalvanicBombardment(), new TakeInventory()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gains delirium immediately when a fourth card type enters the graveyard")
    void gainsDeliriumWhileOnBattlefield() {
        Permanent survivalists = addSurvivalists(List.of(
                new LupinePrototype(), new GalvanicBombardment()));
        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();

        harness.setGraveyard(player1, List.of(
                new LupinePrototype(), new GalvanicBombardment(), new TakeInventory()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addSurvivalists(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return harness.addToBattlefieldAndReturn(player1, new BackwoodsSurvivalists());
    }
}
