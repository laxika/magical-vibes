package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhimsOfTheFates.class, GrizzlyBears.class, GiantSpider.class, Forest.class})
class WhimsOfTheFatesTest extends BaseCardTest {

    @Test
    @DisplayName("Each player separates permanents starting with the spell's controller")
    void promptsPlayersInControllerFirstOrder() {
        Permanent p1First = addPermanent(player1, new GrizzlyBears());
        addPermanent(player1, new GiantSpider());
        addPermanent(player1, new Forest());
        addPermanent(player2, new GrizzlyBears());
        addPermanent(player2, new GiantSpider());
        addPermanent(player2, new Forest());

        castWhimsOfTheFates();

        PendingInteraction.MultiPermanentChoice choice = activeMultiChoice();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player1, List.of(p1First.getId()));
        choice = activeMultiChoice();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of());
        choice = activeMultiChoice();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("One randomly selected pile is sacrificed for each player")
    void sacrificesOneOfThreePilesForEachPlayer() {
        Permanent p1First = addPermanent(player1, new GrizzlyBears());
        Permanent p1Second = addPermanent(player1, new GiantSpider());
        addPermanent(player1, new Forest());
        Permanent p2First = addPermanent(player2, new GrizzlyBears());
        Permanent p2Second = addPermanent(player2, new GiantSpider());
        addPermanent(player2, new Forest());

        castWhimsOfTheFates();

        harness.handleMultiplePermanentsChosen(player1, List.of(p1First.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Second.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(p2First.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(p2Second.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Piles may be empty")
    void allowsEmptyPiles() {
        Permanent first = addPermanent(player1, new GrizzlyBears());
        Permanent second = addPermanent(player1, new GiantSpider());

        castWhimsOfTheFates();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).size()
                + gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).size()).isIn(0, 2);
    }

    @Test
    @DisplayName("Empty battlefields require no pile choices")
    void resolvesWithNoPermanents() {
        castWhimsOfTheFates();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Whims of the Fates");
    }

    @Test
    @DisplayName("An empty first pile leaves all permanents available for the second pile")
    void allowsEmptyFirstAndSecondPiles() {
        Permanent first = addPermanent(player2, new GrizzlyBears());
        Permanent second = addPermanent(player2, new Forest());

        castWhimsOfTheFates();

        assertThat(activeMultiChoice().playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of());
        assertThat(activeMultiChoice().maxCount()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, second);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).size()).isIn(0, 2);
        assertThat(gd.playerBattlefields.get(player2.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("The second seated player chooses first when they control the spell")
    void startsWithSecondSeatedControllerAndWaitsForAllPiles() {
        Permanent first = addPermanent(player1, new GrizzlyBears());
        Permanent second = addPermanent(player2, new Forest());
        harness.forceActivePlayer(player2);

        castWhimsOfTheFates(player2);

        assertThat(activeMultiChoice().playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));
        assertThat(activeMultiChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).size()
                + gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(2);
    }

    private void castWhimsOfTheFates() {
        castWhimsOfTheFates(player1);
    }

    private void castWhimsOfTheFates(Player caster) {
        harness.setHand(caster, List.of(new WhimsOfTheFates()));
        harness.addMana(caster, ManaColor.RED, 5);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(caster, 0, 0);
    }

    private Permanent addPermanent(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private PendingInteraction.MultiPermanentChoice activeMultiChoice() {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        return choice;
    }
}
