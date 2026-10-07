package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LifeGoesOn;
import com.github.laxika.magicalvibes.cards.i.InfernoJet;
import com.github.laxika.magicalvibes.cards.o.OpenFire;
import com.github.laxika.magicalvibes.cards.r.Redirect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmIntelligence.class, GrizzlyBears.class, LightningBolt.class,
        LifeGoesOn.class, InfernoJet.class, OpenFire.class, Redirect.class})
class SwarmIntelligenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant prompts the optional copy")
    void castingInstantPromptsCopy() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the prompt copies the spell")
    void acceptingCopiesSpell() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        long boltCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(boltCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Declining the prompt leaves only the original spell")
    void decliningDoesNotCopy() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the copy")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void copiesUntargetedInstantWithoutCastingTheCopy() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.passBothPriorities();
        harness.assertLife(player1, 28);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Life Goes On");
    }

    @Test
    void copiesSorceryAndKeepsOriginalTargets() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new InfernoJet()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.passBothPriorities();
        harness.assertLife(player2, 8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseNewTargetWithoutChangingOriginal() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSpellDoesNotTriggerCopy() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player2, List.of(new OpenFire()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void copyKeepsTargetsChangedBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new SwarmIntelligence());
        OpenFire original = new OpenFire();
        harness.setHand(player1, List.of(original));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, original.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
