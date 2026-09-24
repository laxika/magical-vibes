package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueEngineer.class, AvianChangeling.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class PlagueEngineerTest extends BaseCardTest {

    @Test
    void sourceChosenSubtypeFilterAppliesToOpposingCreature() {
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent plagueEngineer = harness.addToBattlefieldAndReturn(player1, new PlagueEngineer());
        plagueEngineer.setChosenSubtype(CardSubtype.BEAR);

        assertThat(gqs.computeStaticBonus(gd, opposingBear).power()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Choosing a creature type gives opposing creatures of that type -1/-1")
    void debuffsOpposingCreaturesOfChosenType() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndChoose("BEAR");

        assertThat(findPermanent(player1, "Plague Engineer").getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The chosen type includes changelings and can kill a 1/1 opponent creature")
    void debuffIncludesChangelingsAndKillsOneOne() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castAndChoose("WIZARD");

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Fugitive Wizard"));
    }

    private void castAndChoose(String creatureType) {
        harness.setHand(player1, List.of(new PlagueEngineer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, creatureType);
    }
}
