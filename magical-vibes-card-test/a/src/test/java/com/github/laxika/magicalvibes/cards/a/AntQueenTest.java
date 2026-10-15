package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntQueen.class, Unsummon.class})
class AntQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Ant Queen");
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 green Insect token")
    void resolvingAbilityCreatesToken() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent token = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Insect"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
    }

    @Test
    @DisplayName("Insect token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Insect");
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate ability multiple times with enough mana")
    void canActivateMultipleTimes() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = countPermanents(player1, "Insect");
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ant Queen remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ant Queen");
    }

    @Test
    @DisplayName("Tapped summoning-sick Ant Queen can activate without tapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new AntQueen());
        queen.setSummoningSick(true);
        queen.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(queen.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Ability requires green mana even when enough generic mana is available")
    void cannotActivateWithoutGreenMana() {
        addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("Ability still creates its controller's token after Ant Queen is returned to hand")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent queen = addAntQueenReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, queen.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ant Queen")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(countPermanents(player2, "Insect")).isZero();
    }

    private Permanent addAntQueenReady(Player player) {
        return addCreatureReady(player, new AntQueen());
    }
}
