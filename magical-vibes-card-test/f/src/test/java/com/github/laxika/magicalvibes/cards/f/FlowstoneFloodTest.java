package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneFlood.class, CityOfTraitors.class, RagingGoblin.class, Forbid.class})
class FlowstoneFloodTest extends BaseCardTest {

    @Test
    @DisplayName("Destroying a land without buyback puts Flowstone Flood in the graveyard")
    void destroysLandWithoutBuyback() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        harness.setHand(player1, List.of(spell));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Buyback pays life, randomly discards, destroys the land, and returns Flowstone Flood")
    void buybackPaysLifeDiscardsAndReturnsToHand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        Card discarded = new RagingGoblin();
        harness.setHand(player1, List.of(spell, discarded));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        harness.castSorceryWithBuyback(player1, 0, land.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Buyback cannot be paid without a card to discard")
    void buybackRequiresRandomDiscard() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        harness.setHand(player1, List.of(spell));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flowstone Flood cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new FlowstoneFlood()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flowstone Flood can destroy a land its caster controls")
    void canTargetOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors());
        harness.setHand(player1, List.of(new FlowstoneFlood()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
    }

    @Test
    @DisplayName("Buyback randomly discards exactly one card when multiple cards are available")
    void buybackRandomlyDiscardsExactlyOneCard() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        RagingGoblin first = new RagingGoblin();
        RagingGoblin second = new RagingGoblin();
        harness.setHand(player1, List.of(spell, first, second));
        addMana();

        harness.castSorceryWithBuyback(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .contains(spell)
                .containsAnyOf(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .containsAnyOf(first, second);
    }

    @Test
    @DisplayName("Buyback cannot be paid without enough life")
    void buybackRequiresEnoughLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        RagingGoblin discard = new RagingGoblin();
        harness.setHand(player1, List.of(spell, discard));
        addMana();
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell, discard);
    }

    @Test
    @DisplayName("Countering Flowstone Flood prevents buyback without refunding costs")
    void counteredSpellDoesNotReturnToHand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        RagingGoblin discard = new RagingGoblin();
        harness.setHand(player1, List.of(spell, discard));
        harness.setHand(player2, List.of(new Forbid()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castSorceryWithBuyback(player1, 0, land.getId());
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, discard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Buyback does not return Flowstone Flood when its only target is gone")
    void illegalTargetPreventsBuybackReturn() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        FlowstoneFlood spell = new FlowstoneFlood();
        RagingGoblin discard = new RagingGoblin();
        harness.setHand(player1, List.of(spell, discard));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        harness.castSorceryWithBuyback(player1, 0, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, discard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
