package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadameWebClairvoyant.class, GiantSpider.class, GrizzlyBears.class, Shock.class})
class MadameWebClairvoyantTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a Spider spell from the top of the library")
    void castsSpiderSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new MadameWebClairvoyant());
        Card spider = new GiantSpider();
        harness.setLibrary(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(spider);
    }

    @Test
    @DisplayName("Can cast a noncreature spell from the top of the library")
    void castsNoncreatureSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new MadameWebClairvoyant());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Cannot cast a non-Spider creature from the top of the library")
    void cannotCastNonSpiderCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new MadameWebClairvoyant());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
    }

    @Test
    @DisplayName("When you attack, you may mill one card once per combat")
    void mayMillOneCardWhenAttacking() {
        harness.addToBattlefield(player1, new MadameWebClairvoyant());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Does not trigger when an opponent attacks")
    void doesNotTriggerForOpponentAttack() {
        harness.addToBattlefield(player1, new MadameWebClairvoyant());
        addCreatureReady(player2, new GrizzlyBears());
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
