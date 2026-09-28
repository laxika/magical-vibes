package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourWishIsMyCommand.class, Opt.class, GrizzlyBears.class, Forest.class})
class YourWishIsMyCommandTest extends BaseCardTest {

    @Test
    void offersOnlyInstantAndSorcerySideboardCards() {
        Card instant = new Opt();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant, creature, land)));

        castWish();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).singleElement().satisfies(ability ->
                assertThat(ability.targetCardId()).isEqualTo(instant.getId()));
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(instant, creature, land);
    }

    @Test
    void acceptsAnInstantAndCastsItForItsNormalCost() {
        Card instant = new Opt();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant)));

        castWish();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(instant.getId());
        assertThat(gd.stack.getLast().getCard()).isSameAs(instant);
        assertThat(gd.stack.getLast().getSourceZone()).isEqualTo(Zone.OUTSIDE_GAME);
    }

    @Test
    void decliningLeavesTheSideboardCardOutsideTheGame() {
        Card instant = new Opt();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant)));

        castWish();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(instant.getId());
    }

    private void castWish() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new YourWishIsMyCommand(), "{1}{U}");
        harness.passBothPriorities();
    }
}
