package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MandateOfAbaddon.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, Plains.class,
        GiantGrowth.class, Unsummon.class})
class MandateOfAbaddonTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with power less than the target's power")
    void destroysCreaturesWithLessPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new HillGiant());
        var target = harness.getPermanentId(player1, "Hill Giant");

        harness.setHand(player1, List.of(new MandateOfAbaddon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, target);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Uses the target's power at resolution after a response increases it")
    void usesTargetsPowerAtResolution() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new HillGiant());
        var target = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(new MandateOfAbaddon(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target);
        harness.castAndResolveInstant(player1, 0, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Uses other creatures' current power when deciding which to destroy")
    void usesOtherCreaturesPowerAtResolution() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        var target = harness.getPermanentId(player1, "Air Elemental");
        var bears = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new MandateOfAbaddon(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target);
        harness.castAndResolveInstant(player1, 0, bears);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not destroy any creatures if its target leaves before resolution")
    void doesNotResolveWhenTargetLeaves() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var target = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(new MandateOfAbaddon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target);
        harness.castAndResolveInstant(player1, 0, target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mandate of Abaddon");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new MandateOfAbaddon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var target = harness.getPermanentId(player2, "Hill Giant");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new MandateOfAbaddon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var target = harness.getPermanentId(player1, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
