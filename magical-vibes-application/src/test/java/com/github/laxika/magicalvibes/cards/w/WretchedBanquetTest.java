package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WretchedBanquet.class, GrizzlyBears.class, HillGiant.class, GiantSpider.class,
        Forest.class, GiantGrowth.class})
class WretchedBanquetTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature that uniquely has the least power")
    void destroysUniqueLeastPower() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(); // power 2 (least)
        harness.addToBattlefield(player2, new HillGiant()); // power 3
        castWretchedBanquet(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Destroys a creature tied for least power")
    void destroysTiedForLeastPower() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(); // power 2
        harness.addToBattlefield(player2, new GiantSpider()); // power 2 (tied least)
        harness.addToBattlefield(player2, new HillGiant()); // power 3
        castWretchedBanquet(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when the target does not have the least power")
    void survivesWhenNotLeastPower() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId(); // power 3
        harness.addToBattlefield(player2, new GrizzlyBears()); // power 2 (the least)
        castWretchedBanquet(target);

        // Hill Giant is not tied for least power, so the destroy is skipped at resolution.
        harness.assertNotInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID land = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new WretchedBanquet()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys the only creature even when noncreature permanents are present")
    void destroysOnlyCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.addToBattlefield(player1, new Forest());

        castWretchedBanquet(target);

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A smaller creature controlled by the caster prevents destruction")
    void comparesCreaturesAcrossControllers() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());

        castWretchedBanquet(target);

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy the caster's own creature tied for least power")
    void destroysOwnCreatureTiedAcrossControllers() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new GiantSpider());

        castWretchedBanquet(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Target survives after a response raises its power above another creature")
    void checksTargetsEffectivePowerAtResolution() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new WretchedBanquet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, List.of(target));

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Wretched Banquet");
    }

    @Test
    @DisplayName("A target that was not least at casting can become least before resolution")
    void destroysTargetThatBecomesLeastPower() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        UUID smaller = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new WretchedBanquet(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(target));

        harness.castAndResolveInstant(player1, 0, smaller);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castWretchedBanquet(UUID targetId) {
        harness.setHand(player1, List.of(new WretchedBanquet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));
    }
}
