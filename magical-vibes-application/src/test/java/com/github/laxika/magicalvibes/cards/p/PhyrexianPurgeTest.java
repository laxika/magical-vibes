package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Deathgreeter;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianPurge.class, FeralShadow.class, IronTuskElephant.class, Deathgreeter.class, Forest.class})
class PhyrexianPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target creatures and costs 6 life")
    void destroysTwoCreaturesForSixLife() {
        harness.addToBattlefield(player2, new FeralShadow());
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");
        UUID elephantId = harness.getPermanentId(player2, "Iron Tusk Elephant");

        harness.castSorcery(player1, 0, List.of(shadowId, elephantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Feral Shadow");
        harness.assertInGraveyard(player2, "Iron Tusk Elephant");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster and costs 3 life")
    void destroysCreatureControlledByCaster() {
        harness.addToBattlefield(player1, new FeralShadow());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID shadowId = harness.getPermanentId(player1, "Feral Shadow");

        harness.castSorcery(player1, 0, List.of(shadowId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Feral Shadow");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Targets are destroyed simultaneously, so each dying watcher sees the others die")
    void destroysTargetsSimultaneously() {
        // Both Deathgreeters are destroyed by the same spell. Because the destruction is
        // simultaneous, each one still sees the other die and its "whenever another creature dies"
        // trigger fires — destroying them one at a time would only fire the second one's trigger.
        harness.addToBattlefield(player2, new Deathgreeter());
        harness.addToBattlefield(player2, new Deathgreeter());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        List<UUID> deathgreeterIds = findPermanents(player2, "Deathgreeter").stream()
                .map(Permanent::getId)
                .toList();

        harness.castSorcery(player1, 0, deathgreeterIds);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Deathgreeter");

        // One "may gain 1 life" trigger per Deathgreeter, each watching the other die.
        int accepted = 0;
        for (int i = 0; i < 10; i++) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player2, true);
                accepted++;
            } else if (gd.stack.isEmpty()) {
                break;
            }
            harness.passBothPriorities();
        }

        assertThat(accepted).isEqualTo(2);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Casting with no targets costs no life and destroys nothing")
    void noTargetsCostsNoLife() {
        harness.addToBattlefield(player2, new FeralShadow());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Feral Shadow");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot cast with more targets than life can pay for")
    void cannotPayLifeForTargets() {
        harness.addToBattlefield(player2, new FeralShadow());
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 5);

        UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");
        UUID elephantId = harness.getPermanentId(player2, "Iron Tusk Elephant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(shadowId, elephantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("life");
        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
