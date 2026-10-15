package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.w.WortTheRaidmother;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebornHope.class, WortTheRaidmother.class, GrizzlyBears.class, MycosynthLattice.class})
class RebornHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target multicolored card from your graveyard to your hand")
    void returnsMulticoloredCardToHand() {
        Card multicolored = new WortTheRaidmother();
        harness.setGraveyard(player1, List.of(multicolored));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, multicolored.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(multicolored.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(multicolored.getId()));
    }

    @Test
    @DisplayName("Cannot target a monocolored card in your graveyard")
    void cannotTargetMonocoloredCard() {
        Card monocolored = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(monocolored));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, monocolored.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card multicolored = new WortTheRaidmother();
        harness.setGraveyard(player2, List.of(multicolored));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, multicolored.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Returns only the chosen multicolored noncreature card")
    void returnsOnlyChosenNoncreatureCard() {
        Card target = new RebornHope();
        Card other = new RebornHope();
        Card spell = new RebornHope();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(target);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(other, spell);
    }

    @Test
    @DisplayName("Cannot cast without a required target")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new RebornHope()));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return another card when the target leaves the graveyard")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new RebornHope();
        Card other = new RebornHope();
        Card spell = new RebornHope();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(other, spell);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a printed multicolored card made colorless by Mycosynth Lattice")
    void cannotTargetCardMadeColorless() {
        Card target = new RebornHope();
        harness.addToBattlefield(player2, new MycosynthLattice());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return a target that becomes colorless before resolution")
    void doesNotReturnTargetThatBecomesColorless() {
        Card target = new RebornHope();
        Card spell = new RebornHope();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.addToBattlefield(player2, new MycosynthLattice());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(target, spell);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
