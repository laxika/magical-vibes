package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PureSimple.class, BorosSwiftblade.class, GrizzlyBears.class, Bonesplitter.class,
        DarksteelRelic.class, GloriousAnthem.class, Pacifism.class})
class PureSimpleTest extends BaseCardTest {

    private static final int PURE = 0;
    private static final int SIMPLE = 1;

    @Test
    @DisplayName("Pure destroys a target multicolored permanent")
    void pureDestroysTargetMulticoloredPermanent() {
        Permanent multicolored = harness.addToBattlefieldAndReturn(player2, new BorosSwiftblade());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, PURE, List.of(multicolored.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boros Swiftblade");
        harness.assertInGraveyard(player2, "Boros Swiftblade");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pure cannot target a monocolored permanent")
    void pureCannotTargetMonocoloredPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, PURE, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored");
    }

    @Test
    @DisplayName("Simple destroys all Auras and Equipment but leaves other permanents")
    void simpleDestroysAurasAndEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorcery(player1, 0, SIMPLE, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
