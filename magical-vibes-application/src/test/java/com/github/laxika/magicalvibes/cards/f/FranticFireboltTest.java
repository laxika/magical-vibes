package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.g.GarenbrigCarver;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FranticFirebolt.class, Duress.class, FountainOfYouth.class, GarenbrigCarver.class,
        LlanowarElves.class, Shock.class, YavimayaWurm.class})
class FranticFireboltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals two damage with no qualifying cards in the graveyard")
    void dealsBaseDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        castFranticFirebolt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts instant, sorcery, and Adventure cards in the graveyard")
    void damageScalesWithQualifyingGraveyardCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        List<Card> graveyard = List.of(
                new Shock(),
                new Duress(),
                new GarenbrigCarver(),
                new LlanowarElves()
        );
        harness.setGraveyard(player1, graveyard);
        castFranticFirebolt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Requires a creature target")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FranticFirebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        var targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFranticFirebolt(Permanent target) {
        harness.setHand(player1, List.of(new FranticFirebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
