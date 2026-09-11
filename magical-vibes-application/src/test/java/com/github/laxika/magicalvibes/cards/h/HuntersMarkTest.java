package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntersMark.class, Cancel.class, CoralMerfolk.class, GrizzlyBears.class, HillGiant.class})
class HuntersMarkTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the source creature before it deals damage equal to its power")
    void boostsSourceBeforeDealingPowerDamage() {
        Permanent source = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Costs only {G} when its second target is a blue permanent an opponent controls")
    void reducedCostForBlueOpponentTarget() {
        Permanent source = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new CoralMerfolk());
        gd.playerBattlefields.get(player1.getId()).add(source);
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Requires the full cost when the second target is not blue")
    void fullCostForNonBlueOpponentTarget() {
        Permanent source = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce the cost for a blue first target you control")
    void blueFirstTargetYouControlDoesNotReduceCost() {
        Permanent source = new Permanent(new CoralMerfolk());
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent source = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(source);
        gd.playerBattlefields.get(player1.getId()).add(victim);
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentsCreatureAsFirstTarget() {
        Permanent source = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(source);
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        HuntersMark mark = new HuntersMark();
        Permanent source = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new CoralMerfolk());
        gd.playerBattlefields.get(player1.getId()).add(source);
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(mark));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mark.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hunter's Mark");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player2, "Coral Merfolk");
    }
}
