package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Flight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flight.class, GrizzlyBears.class, StudentOfElements.class})
class StudentOfElementsTest extends BaseCardTest {

    private Permanent addStudent() {
        Permanent student = new Permanent(new StudentOfElements());
        student.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(student);
        return student;
    }

    @Test
    @DisplayName("Stays unflipped while it doesn't have flying")
    void staysUnflippedWithoutFlying() {
        Permanent student = addStudent();

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(student.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips into Tobita when it gains flying")
    void flipsWhenItGainsFlying() {
        Permanent student = addStudent();
        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, student.getId());
        harness.passBothPriorities(); // Flight resolves -> state trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves -> flipped

        assertThat(student.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Once flipped, Tobita gives every creature you control flying")
    void flippedGrantsFlyingToOwnCreatures() {
        Permanent student = addStudent();
        student.setTransformed(true);
        student.setCard(student.getOriginalCard().getBackFaceCard());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownBears = gd.playerBattlefields.get(player1.getId()).getLast();
        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).getLast();

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, student, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FLYING)).isFalse();
    }
}
