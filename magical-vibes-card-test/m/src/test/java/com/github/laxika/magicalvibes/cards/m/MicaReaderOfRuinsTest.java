package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MicaReaderOfRuins.class, IronMyr.class, LightningBolt.class, GrizzlyBears.class,
        Earthquake.class, IcyManipulator.class})
class MicaReaderOfRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("The artifact sacrifice choice is made only when the trigger resolves")
    void castingInstantPromptsArtifactSacrifice() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting and sacrificing an artifact creates a copy")
    void sacrificingArtifactCreatesCopy() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var artifact = findPermanent(player1, "Iron Myr");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }

        harness.assertNotOnBattlefield(player1, "Iron Myr");
        long boltCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(boltCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Declining does not sacrifice an artifact or create a copy")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Iron Myr");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells do not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ward counters an opposing spell when its controller declines to pay life")
    void wardCountersOpposingSpell() {
        var mica = harness.addToBattlefieldAndReturn(player1, new MicaReaderOfRuins());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, mica.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(mica.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Paying exactly 3 life allows an opposing spell to resolve")
    void payingWardAllowsSpellToResolve() {
        var mica = harness.addToBattlefieldAndReturn(player1, new MicaReaderOfRuins());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, mica.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(mica.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward also counters an opposing activated ability")
    void wardCountersOpposingActivatedAbility() {
        var mica = harness.addToBattlefieldAndReturn(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, mica.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(mica.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice and copying happen during the same ability resolution")
    void sacrificeCreatesCopyWithoutAnotherResponseWindow() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        var artifact = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.stack).hasSize(2)
                .allMatch(entry -> entry.getCard().getName().equals("Lightning Bolt"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Iron Myr");
    }

    @Test
    @DisplayName("An opponent's instant does not trigger the artifact sacrifice ability")
    void opponentInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertOnBattlefield(player1, "Iron Myr");
    }

    @Test
    @DisplayName("With no artifact to sacrifice, no copy is created")
    void noArtifactMeansNoCopy() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        harness.addToBattlefield(player2, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Iron Myr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A copied sorcery retains X and is not cast again")
    void sorceryCopyRetainsXWithoutRetriggering() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        var artifact = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Iron Myr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The copy can choose a new target while the original retains its target")
    void copyCanChooseNewTarget() {
        harness.addToBattlefield(player1, new MicaReaderOfRuins());
        var artifact = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }
}
