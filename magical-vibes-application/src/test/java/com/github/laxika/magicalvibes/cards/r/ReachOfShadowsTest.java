package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AleshasVanguard;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.m.MantisRider;
import com.github.laxika.magicalvibes.cards.m.MonasterySiege;
import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReachOfShadows.class, BronzeSable.class, MantisRider.class,
        AleshasVanguard.class, MonasterySiege.class, SoulSummons.class})
class ReachOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a multicolored creature")
    void destroysMulticoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MantisRider());

        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Mantis Rider");
        harness.assertInGraveyard(player2, "Mantis Rider");
    }

    @Test
    @DisplayName("Cannot target a colorless creature")
    void cannotTargetColorlessCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BronzeSable());

        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one or more colors");
        harness.assertOnBattlefield(player2, "Bronze Sable");
    }

    @Test
    @DisplayName("Can destroy its controller's monocolored creature")
    void destroysOwnMonocoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AleshasVanguard());
        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Alesha's Vanguard");
        harness.assertInGraveyard(player1, "Alesha's Vanguard");
    }

    @Test
    @DisplayName("Cannot target a colored noncreature permanent")
    void cannotTargetColoredNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MonasterySiege());
        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one or more colors");

        harness.assertOnBattlefield(player2, "Monastery Siege");
    }

    @Test
    @DisplayName("A manifested colored card is illegal face down but legal after turning face up")
    void manifestedCreatureMustBeFaceUp() {
        harness.setLibrary(player1, List.of(new AleshasVanguard()));
        harness.setHand(player1, List.of(new SoulSummons(), new ReachOfShadows()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLACK, 9);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one or more colors");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.turnFaceUp(player1, 0);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Alesha's Vanguard");
        harness.assertInGraveyard(player1, "Alesha's Vanguard");
    }

    @Test
    @DisplayName("Does not destroy another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AleshasVanguard());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AleshasVanguard());
        harness.setHand(player1, List.of(new ReachOfShadows(), new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player2, "Alesha's Vanguard");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof ReachOfShadows).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
