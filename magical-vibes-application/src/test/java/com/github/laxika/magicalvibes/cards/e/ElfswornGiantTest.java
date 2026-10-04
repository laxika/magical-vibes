package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElfswornGiant.class, Forest.class})
class ElfswornGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 1/1 Elf Warrior token when a land enters under your control")
    void landfallCreatesElfWarrior() {
        harness.addToBattlefield(player1, new ElfswornGiant());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent elfWarrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Elf Warrior"))
                .findFirst()
                .orElseThrow();
        assertThat(elfWarrior.getEffectivePower()).isEqualTo(1);
        assertThat(elfWarrior.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A land entering under an opponent's control does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElfswornGiant());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Elf Warrior"))
                .count()).isZero();
    }

    @Test
    @DisplayName("A land entering without being played creates an untapped green Elf Warrior creature")
    void landEnteringWithoutBeingPlayedCreatesToken() {
        harness.addToBattlefield(player1, new ElfswornGiant());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.isAttacking()).isFalse();
                });
    }

    @Test
    @DisplayName("Each land entering in the same turn creates another token")
    void repeatedLandEntriesEachTrigger() {
        harness.addToBattlefield(player1, new ElfswornGiant());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Each Elfsworn Giant creates its own token for one entering land")
    void multipleGiantsTriggerIndependently() {
        harness.addToBattlefield(player1, new ElfswornGiant());
        harness.addToBattlefield(player1, new ElfswornGiant());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }
}
