package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DemonicPact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravenArchfiend.class, DemonicPact.class, GrizzlyBears.class, Island.class})
class GravenArchfiendTest extends BaseCardTest {

    @Test
    void doesNotConjureDemonicPactWhenAdditionalCostIsNotPaid() {
        castGravenArchfiend();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Graven Archfiend");
        harness.assertNotOnBattlefield(player1, "Demonic Pact");
    }

    @Test
    void conjuresDemonicPactWhenNonDemonCreatureIsSacrificed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GravenArchfiend()));
        addGravenArchfiendMana();

        castGravenArchfiendWithSacrifice(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Demonic Pact");
    }

    @Test
    void cannotSacrificeADemonOrNoncreature() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new GravenArchfiend());
        harness.setHand(player1, List.of(new GravenArchfiend()));
        addGravenArchfiendMana();

        assertThatThrownBy(() -> castGravenArchfiendWithSacrifice(demon.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GravenArchfiend()));
        addGravenArchfiendMana();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> castGravenArchfiendWithSacrifice(island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGravenArchfiend() {
        harness.setHand(player1, List.of(new GravenArchfiend()));
        addGravenArchfiendMana();
        harness.castCreature(player1, 0);
    }

    private void castGravenArchfiendWithSacrifice(UUID sacrificeId) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrificeId);
    }

    private void addGravenArchfiendMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
