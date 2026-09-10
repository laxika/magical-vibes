package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DauthiJackal;
import com.github.laxika.magicalvibes.cards.m.MemoryCrystal;
import com.github.laxika.magicalvibes.cards.m.MoggAssassin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Plaguebearer.class, MoggAssassin.class, DauthiJackal.class, MemoryCrystal.class})
class PlaguebearerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature with mana value X")
    void destroysNonblackCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new MoggAssassin());
        UUID target = harness.getPermanentId(player2, "Mogg Assassin");
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.activateAbility(player1, 0, 3, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Assassin");
        harness.assertInGraveyard(player2, "Mogg Assassin");
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value is not X")
    void cannotTargetCreatureWithDifferentManaValue() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new MoggAssassin());
        UUID target = harness.getPermanentId(player2, "Mogg Assassin");
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new DauthiJackal());
        UUID target = harness.getPermanentId(player2, "Dauthi Jackal");
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new MemoryCrystal());
        UUID target = harness.getPermanentId(player2, "Memory Crystal");
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
