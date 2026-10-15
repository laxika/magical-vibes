package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FinalReward;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProwlingSerpopard.class, Cancel.class, GrizzlyBears.class, GiantSpider.class,
        HazeOfPollen.class, FinalReward.class})
class ProwlingSerpopardTest extends BaseCardTest {

    @Test
    @DisplayName("Prowling Serpopard cannot be countered by Cancel")
    void cannotBeCountered() {
        ProwlingSerpopard serpopard = new ProwlingSerpopard();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, serpopard, "{1}{G}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, serpopard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prowling Serpopard");
        harness.assertNotInGraveyard(player1, "Prowling Serpopard");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Other creature spells the controller casts cannot be countered")
    void protectsOtherCreatureSpells() {
        harness.addToBattlefield(player1, new ProwlingSerpopard());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Prowling Serpopard does not protect an opponent's creature spells")
    void doesNotProtectOpponentCreatureSpells() {
        harness.addToBattlefield(player2, new ProwlingSerpopard());
        GiantSpider spider = new GiantSpider();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, spider, "{3}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spider.getId());

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Prowling Serpopard does not protect its controller's noncreature spells")
    void doesNotProtectNoncreatureSpells() {
        harness.addToBattlefield(player1, new ProwlingSerpopard());
        HazeOfPollen haze = new HazeOfPollen();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, haze, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, haze.getId());

        harness.assertInGraveyard(player1, "Haze of Pollen");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertOnBattlefield(player1, "Prowling Serpopard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    @DisplayName("Creature spells lose protection when Serpopard leaves before the counterspell resolves")
    void protectionEndsWhenSerpopardLeavesBattlefield() {
        var serpopard = harness.addToBattlefieldAndReturn(player1, new ProwlingSerpopard());
        GiantSpider spider = new GiantSpider();
        harness.setHand(player2, List.of(new Cancel(), new FinalReward()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castFromHand(player1, spider, "{3}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        harness.castAndResolveInstant(player2, 0, serpopard.getId());
        harness.assertNotOnBattlefield(player1, "Prowling Serpopard");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player2, "Final Reward");
    }
}
