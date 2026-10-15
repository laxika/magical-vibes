package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwynensElite.class, LeafGilder.class, Disperse.class})
class DwynensEliteTest extends BaseCardTest {

    @Test
    @DisplayName("With another Elf, the ETB creates a 1/1 green Elf Warrior token")
    void etbCreatesTokenWithAnotherElf() {
        harness.addToBattlefield(player1, new LeafGilder());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Without another Elf, no token is created (the Elite itself does not count)")
    void noTokenWithoutAnotherElf() {
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dwynen's Elite");
        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("An opponent's Elf does not satisfy the intervening-if")
    void opponentElfDoesNotCount() {
        harness.addToBattlefield(player2, new LeafGilder());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("No ability triggers when there is no other Elf at entry")
    void noTriggerWithoutAnotherElfAtEntry() {
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwynen's Elite");
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new LeafGilder());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("The token is not created if the last other Elf leaves before resolution")
    void conditionIsRecheckedAtResolution() {
        harness.addToBattlefield(player1, new LeafGilder());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Leaf Gilder"));
        resolveAllTriggers();

        harness.assertInHand(player1, "Leaf Gilder");
        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("The ability still creates a token if the Elite leaves but another Elf remains")
    void sourceCanLeaveBeforeResolution() {
        harness.addToBattlefield(player1, new LeafGilder());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dwynen's Elite"));
        resolveAllTriggers();

        harness.assertInHand(player1, "Dwynen's Elite");
        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("Multiple other Elves still produce exactly one token")
    void multipleElvesCreateOnlyOneToken() {
        harness.addToBattlefield(player1, new LeafGilder());
        harness.addToBattlefield(player1, new LeafGilder());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elf Warrior")).isZero();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }
}
