package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.ExpelFromOrazca;
import com.github.laxika.magicalvibes.cards.o.OrazcaRelic;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Impale.class, RaptorCompanion.class, OrazcaRelic.class, ExpelFromOrazca.class})
class ImpaleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature")
    void destroysTargetCreature() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new Impale()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Raptor Companion"));

        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new OrazcaRelic());
        harness.setHand(player1, List.of(new Impale()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Orazca Relic")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by its caster")
    void destroysOwnCreature() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new Impale()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Raptor Companion"));

        harness.assertNotOnBattlefield(player1, "Raptor Companion");
        harness.assertInGraveyard(player1, "Raptor Companion");
    }

    @Test
    @DisplayName("Does not destroy another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.addToBattlefield(player1, new RaptorCompanion());
        UUID targetId = harness.getPermanentId(player2, "Raptor Companion");
        harness.setHand(player1, List.of(new Impale()));
        harness.setHand(player2, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Raptor Companion");
        harness.assertNotInGraveyard(player2, "Raptor Companion");
        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertInGraveyard(player1, "Impale");
    }
}
