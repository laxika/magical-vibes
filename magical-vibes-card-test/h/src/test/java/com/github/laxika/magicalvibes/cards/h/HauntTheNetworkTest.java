package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DebrisBeetle;
import com.github.laxika.magicalvibes.cards.w.WretchedDoll;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntTheNetwork.class, WretchedDoll.class, DebrisBeetle.class})
class HauntTheNetworkTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Thopters for you and counts them along with your existing artifacts")
    void createsThoptersAndDrainsForControlledArtifacts() {
        harness.addToBattlefield(player1, new WretchedDoll());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
            assertThat(thopter.getCard().getColors()).isEmpty();
            assertThat(thopter.getEffectivePower()).isEqualTo(1);
            assertThat(thopter.getEffectiveToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target the controller")
    void cannotTargetController() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With no previous artifacts, the newly created Thopters drain two life")
    void countsNewTokensWithNoPreviousArtifacts() {
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Counts noncreature artifacts but excludes the opponent's artifacts")
    void countsOnlyArtifactsYouControlIncludingNoncreatures() {
        harness.addToBattlefield(player1, new DebrisBeetle());
        harness.addToBattlefield(player2, new WretchedDoll());
        harness.addToBattlefield(player2, new WretchedDoll());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Determines the artifact count on resolution rather than when cast")
    void countsArtifactsAtResolution() {
        prepareSpell();
        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new WretchedDoll());

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Gains the full artifact count even when the drain makes the opponent's life negative")
    void gainsFullAmountWhenOpponentHasLessLife() {
        prepareSpell();
        harness.setLife(player2, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 22);
        harness.assertLife(player2, -1);
    }

    private void prepareSpell() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HauntTheNetwork()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
