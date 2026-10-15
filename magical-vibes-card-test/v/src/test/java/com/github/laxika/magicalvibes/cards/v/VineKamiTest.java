package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KokushoTheEveningStar;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.t.ThousandLeggedKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VineKami.class, LanternKami.class, KokushoTheEveningStar.class,
        ThousandLeggedKami.class, SakuraTribeElder.class, RendSpirit.class})
class VineKamiTest extends BaseCardTest {

    /** Destroys Vine Kami so its soulshift trigger fires. */
    private void destroyKami() {
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Vine Kami"));
    }

    @Test
    @DisplayName("Soulshift 6 returns a targeted Spirit with mana value 6 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new VineKami());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        destroyKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Expensive Spirits, non-Spirits, and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new VineKami());
        Card cheapSpirit = new LanternKami();
        Card boundarySpirit = new KokushoTheEveningStar();
        Card expensiveSpirit = new ThousandLeggedKami();
        Card nonSpirit = new SakuraTribeElder();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, boundarySpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        destroyKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined after choosing its target")
    void soulshiftMayBeDeclined() {
        harness.addToBattlefield(player1, new VineKami());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        destroyKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("With no Spirit with mana value 6 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new VineKami());
        harness.setGraveyard(player1, List.of(new ThousandLeggedKami()));

        destroyKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Soulshift returns a Spirit with mana value exactly six")
    void returnsSpiritAtManaValueLimit() {
        harness.addToBattlefield(player1, new VineKami());
        Card spirit = new KokushoTheEveningStar();
        harness.setGraveyard(player1, List.of(spirit));

        destroyKami();

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(spirit);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spirit);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new VineKami());
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new VineKami());
        var firstBlocker = addCreatureReady(player2, new SakuraTribeElder());
        var secondBlocker = addCreatureReady(player2, new SakuraTribeElder());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace permits an attack to go unblocked")
    void menaceAllowsNoBlockers() {
        addCreatureReady(player1, new VineKami());
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }
}
