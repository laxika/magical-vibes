package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrototypeX8.class, GrizzlyBears.class})
class PrototypeX8Test extends BaseCardTest {

    @Test
    void costsTwoLessForEachCreatureCardInItsControllersGraveyard() {
        harness.setHand(player1, List.of(new PrototypeX8()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prototype X-8");
    }

    @Test
    void sacrificesACastCreatureAndConjuresANonTokenRobotArtifactDuplicate() {
        GrizzlyBears bearCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new PrototypeX8(), bearCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bearCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .singleElement()
                .satisfies(duplicate -> {
                    assertThat(duplicate.getCard().isToken()).isFalse();
                    assertThat(duplicate.getCard().getId()).isNotEqualTo(bearCard.getId());
                    assertThat(duplicate.getCard().hasType(CardType.ARTIFACT)).isTrue();
                    assertThat(duplicate.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
                });
    }

    @Test
    void doesNotTriggerForACreatureThatEnteredWithoutBeingCast() {
        harness.addToBattlefield(player1, new PrototypeX8());

        Permanent bear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }
}
