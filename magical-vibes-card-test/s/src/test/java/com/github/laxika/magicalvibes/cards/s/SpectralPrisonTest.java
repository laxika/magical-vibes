package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralPrison.class, GrizzlyBears.class, Shock.class, IcyManipulator.class, Naturalize.class,
        SeedbornMuse.class})
class SpectralPrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Spectral Prison attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpectralPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spectral Prison")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        attachPrison(player1, creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        enchanted.tap();
        Permanent free = addCreatureReady(player2, new GrizzlyBears());
        free.tap();

        attachPrison(player1, enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again once Spectral Prison leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent prison = attachPrison(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(prison);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Spectral Prison is sacrificed when the enchanted creature becomes the target of a spell")
    void sacrificedWhenEnchantedCreatureTargetedBySpell() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPrison(player1, creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        // Shock plus Spectral Prison's triggered ability on top of it
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spectral Prison");
        harness.assertInGraveyard(player1, "Spectral Prison");
    }

    @Test
    @DisplayName("Spectral Prison is not sacrificed when the enchanted creature becomes the target of an ability")
    void notSacrificedWhenEnchantedCreatureTargetedByAbility() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPrison(player1, creature);

        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icy.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(icy), null, creature.getId());

        harness.passBothPriorities();

        // Only spells trigger the sacrifice — the Aura survives an ability targeting its host
        harness.assertOnBattlefield(player1, "Spectral Prison");
    }

    @Test
    @DisplayName("Targeting Spectral Prison itself does not trigger the sacrifice")
    void notTriggeredWhenPrisonItselfIsTargeted() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent prison = attachPrison(player1, creature);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, prison.getId());

        // Naturalize alone — no sacrifice trigger stacked on top of it
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Attaching Spectral Prison does not tap an untapped creature")
    void attachingDoesNotTapCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Spectral Prison");
    }

    @Test
    @DisplayName("An opponent's spell sacrifices the Prison before that spell resolves")
    void opponentsSpellTriggersSacrificeBeforeResolving() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPrison(player1, creature);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spectral Prison");
        harness.assertNotOnBattlefield(player1, "Spectral Prison");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell targeting another creature does not sacrifice Spectral Prison")
    void targetingAnotherCreatureDoesNotTriggerSacrifice() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        attachPrison(player1, enchanted);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, other.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spectral Prison");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted).doesNotContain(other);
    }

    @Test
    @DisplayName("Spectral Prison allows Seedborn Muse to untap the host during another player's untap step")
    void hostUntapsDuringOtherPlayersUntapStep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.addToBattlefield(player2, new SeedbornMuse());
        attachPrison(player1, creature);

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Spectral Prison");
    }

    @Test
    @DisplayName("Another Aura spell targeting the enchanted creature sacrifices only the old Prison")
    void auraSpellTriggersSacrifice() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent oldPrison = attachPrison(player1, creature);
        harness.setHand(player1, List.of(new SpectralPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oldPrison);
        harness.assertInGraveyard(player1, "Spectral Prison");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent newPrison = findPermanent(player1, "Spectral Prison");
        assertThat(newPrison.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(newPrison.getId()).isNotEqualTo(oldPrison.getId());
    }

    private Permanent attachPrison(Player controller, Permanent creature) {
        Permanent prison = harness.addToBattlefieldAndReturn(controller, new SpectralPrison());
        prison.setAttachedTo(creature.getId());
        return prison;
    }

}
