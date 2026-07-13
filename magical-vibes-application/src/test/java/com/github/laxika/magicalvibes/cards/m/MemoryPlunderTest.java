package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoryPlunderTest extends BaseCardTest {

    @Test
    @DisplayName("Casts targeted instant from opponent's graveyard for free and prompts for its target")
    void castsTargetedInstantFromOpponentGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities(); // resolve Memory Plunder → queues may-cast

        harness.handleMayAbilityChosen(player1, true);

        // Shock needs a target
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve Shock → 2 damage to Grizzly Bears

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Casts non-targeted sorcery from opponent's graveyard without paying its mana cost")
    void castsNonTargetedSorceryFromOpponentGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, counsel.getId());
        harness.passBothPriorities(); // resolve Memory Plunder → queues may-cast

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve Counsel of the Soratami → draw 2

        assertThat(gd.playerHands.get(player1.getId())).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may-cast leaves the card in the opponent's graveyard")
    void decliningLeavesCardInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Cannot target an instant in the caster's own graveyard")
    void cannotTargetOwnGraveyard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in the opponent's graveyard")
    void cannotTargetCreatureCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
