package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GoblinWelder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WeatherseedElf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueEngineer.class, AvianChangeling.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class, GoblinWelder.class, WeatherseedElf.class})
class PlagueEngineerTest extends BaseCardTest {

    private Permanent addPlague(CardSubtype chosen) {
        Permanent plague = harness.addToBattlefieldAndReturn(player1, new PlagueEngineer());
        plague.setChosenSubtype(chosen);
        return plague;
    }

    @Test
    @DisplayName("Choosing a creature type on enter stores the chosen type")
    void choosesTypeOnEnter() {
        harness.setHand(player1, List.of(new PlagueEngineer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Plague Engineer").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures of the chosen type an opponent controls get -1/-1")
    void weakensOpponentCreaturesOfChosenType() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Creatures of the chosen type the controller controls are not affected")
    void doesNotWeakenOwnCreatures() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures of a different type are not affected")
    void doesNotAffectOtherTypes() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new WeatherseedElf());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, elf);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature with the chosen type among multiple types is affected")
    void matchesAnyCreatureSubtype() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        addPlague(CardSubtype.ARTIFICER);

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The -1/-1 disappears when Plague Engineer leaves the battlefield")
    void effectRemovedWhenPlagueLeaves() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        Permanent plague = addPlague(CardSubtype.GOBLIN);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(-1);

        gd.playerBattlefields.get(player1.getId()).remove(plague);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
    }
}
