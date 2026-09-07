package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfConvergence.class, Forest.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class CrownOfConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures sharing a color with a creature on top of the library")
    void boostsCreaturesSharingTopCreatureColor() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost creatures when the top card is not a creature")
    void doesNotBoostForNonCreatureTopCard() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A colorless creature on top does not boost any creature")
    void doesNotBoostForColorlessCreatureTopCard() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost follows the current top card")
    void boostFollowsTopCard() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Hill Giant", "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(4);
    }

    private void addCrown() {
        harness.addToBattlefield(player1, new CrownOfConvergence());
    }
}
