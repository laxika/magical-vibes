package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HeirloomEpic.class, GrizzlyBears.class, Forest.class})
class HeirloomEpicTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping four creatures can pay the entire activation cost")
    void tapsCreaturesInsteadOfPayingMana() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        List<Permanent> creatures = addCreatures(4);
        setLibrary(List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creature tapping can pay part of the activation cost")
    void tapsCreaturesAndPaysTheRestWithMana() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        List<Permanent> creatures = addCreatures(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setLibrary(List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The activation cannot tap more creatures than mana in its cost")
    void rejectsTooManyCreatures() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        List<Permanent> creatures = addCreatures(5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, creatures.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many creatures");
        assertThat(creatures).noneMatch(Permanent::isTapped);
    }

    private List<Permanent> addCreatures(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();
    }

    private void setLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
