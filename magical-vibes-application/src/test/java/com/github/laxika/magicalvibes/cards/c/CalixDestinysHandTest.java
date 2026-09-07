package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalixDestinysHand.class, Forest.class, GrizzlyBears.class, PrismaticOmen.class,
        Shock.class, UnderworldDreams.class})
class CalixDestinysHandTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a revealed enchantment into hand and randomizes the rest on the bottom")
    void plusOneFindsEnchantment() {
        Permanent calix = addReadyCalix(4);
        Card enchantment = new PrismaticOmen();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(enchantment, creature, land, instant));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(calix.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land, instant);
    }

    @Test
    @DisplayName("-3 exiles the first target until the second target enchantment leaves")
    void minusThreeUsesTheEnchantmentAsDurationAnchor() {
        Permanent calix = addReadyCalix(4);
        Permanent creature = addPermanent(player2, new GrizzlyBears());
        Permanent enchantment = addPermanent(player1, new PrismaticOmen());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(creature.getId(), enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, calix));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard() == creature.getOriginalCard());
    }

    @Test
    @DisplayName("-3 requires an opposing creature or enchantment and a controlled enchantment")
    void minusThreeTargetRestrictions() {
        addReadyCalix(4);
        Permanent ownCreature = addPermanent(player1, new GrizzlyBears());
        Permanent opposingLand = addPermanent(player2, new Forest());
        Permanent enchantment = addPermanent(player1, new PrismaticOmen());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(ownCreature.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(opposingLand.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 returns all enchantment cards from the controller's graveyard")
    void minusSevenReturnsAllEnchantments() {
        addReadyCalix(7);
        Card firstEnchantment = new PrismaticOmen();
        Card secondEnchantment = new UnderworldDreams();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEnchantment, creature, secondEnchantment));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(firstEnchantment, secondEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(firstEnchantment, secondEnchantment);
    }

    private Permanent addReadyCalix(int loyalty) {
        Permanent perm = new Permanent(new CalixDestinysHand());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
