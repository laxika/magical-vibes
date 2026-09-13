package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbsoluteGrace.class, GorillaWarrior.class})
class AbsoluteGraceTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have protection from black")
    void grantsProtectionFromBlackToAllCreatures() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        harness.addToBattlefield(player1, new GorillaWarrior());
        harness.addToBattlefield(player2, new GorillaWarrior());

        Permanent ownGorilla = findPermanent(player1, "Gorilla Warrior");
        Permanent opponentGorilla = findPermanent(player2, "Gorilla Warrior");

        assertThat(gqs.hasProtectionFrom(gd, ownGorilla, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opponentGorilla, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownGorilla, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection from black ends when Absolute Grace leaves the battlefield")
    void protectionEndsWhenAbsoluteGraceLeaves() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        harness.addToBattlefield(player1, new GorillaWarrior());

        Permanent gorilla = findPermanent(player1, "Gorilla Warrior");
        assertThat(gqs.hasProtectionFrom(gd, gorilla, CardColor.BLACK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Absolute Grace"));

        assertThat(gqs.hasProtectionFrom(gd, gorilla, CardColor.BLACK)).isFalse();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Absolute Grace has protection from black when it becomes a creature")
    void animatedAbsoluteGraceHasProtectionFromBlack() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent grace = harness.addToBattlefieldAndReturn(player1, new AbsoluteGrace());

        assertThat(gqs.isCreature(gd, grace)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, grace, CardColor.BLACK)).isTrue();
    }
}
