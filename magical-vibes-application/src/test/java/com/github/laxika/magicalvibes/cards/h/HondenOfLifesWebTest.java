package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WearAway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HondenOfLifesWeb.class, HondenOfSeeingWinds.class, WearAway.class})
class HondenOfLifesWebTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Spirit token for each Shrine its controller controls")
    void createsSpiritForEachControlledShrine() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        List<Permanent> spirits = spiritTokens();
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        });
    }

    @Test
    @DisplayName("Counts Shrines when the upkeep trigger resolves")
    void recountsShrinesAtResolution() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        resolveAllTriggers();

        assertThat(spiritTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Does not count Shrines controlled by an opponent")
    void ignoresOpponentControlledShrines() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player2, new HondenOfSeeingWinds());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(spiritTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player2, new HondenOfSeeingWinds());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(spiritTokens()).isEmpty();
    }

    @Test
    @DisplayName("A pending trigger creates no tokens when no controlled Shrines remain")
    void createsNoTokensAfterOnlyShrineIsDestroyed() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.setHand(player1, List.of(new WearAway()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Honden of Life's Web"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Honden of Life's Web");
        assertThat(spiritTokens()).isEmpty();
    }

    @Test
    @DisplayName("A pending trigger survives its source and counts the remaining Shrines")
    void createsTokensAfterSourceIsDestroyed() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.setHand(player1, List.of(new WearAway()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Honden of Life's Web"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Honden of Life's Web");
        assertThat(spiritTokens()).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    private List<Permanent> spiritTokens() {
        return findPermanents(player1, "Spirit");
    }
}
