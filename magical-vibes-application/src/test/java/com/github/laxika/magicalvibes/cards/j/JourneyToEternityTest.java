package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JourneyToEternity.class, GrizzlyBears.class, LightningBolt.class, NevinyrralsDisk.class})
class JourneyToEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the enchanted creature and transforms into Atzal when it dies")
    void returnsCreatureAndTransformsOnEnchantedCreatureDeath() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToEternity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        Permanent atzal = findPermanent(player1, "Atzal, Cave of Eternity");
        assertThat(atzal.isTransformed()).isTrue();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotSameAs(creature);
        harness.assertNotInGraveyard(player1, "Journey to Eternity");
    }

    @Test
    @DisplayName("Atzal returns a target creature card from its controller's graveyard")
    void atzalReturnsTargetCreatureFromGraveyard() {
        Permanent atzal = addTransformedAtzal(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCard().getId()).isEqualTo(creature.getId());
        assertThat(atzal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Atzal produces a chosen color of mana")
    void atzalProducesChosenColor() {
        Permanent atzal = addTransformedAtzal(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(atzal.isTapped()).isTrue();
    }

    @Test
    void cannotEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToEternity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Journey to Eternity");
    }

    @Test
    void atzalCannotTargetNoncreatureCard() {
        addTransformedAtzal(player1);
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void atzalCannotTargetOpponentsGraveyard() {
        addTransformedAtzal(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotReturnCreatureThatWasReanimatedAndDiedAgainBeforeTriggerResolved() {
        Permanent existingAtzal = addTransformedAtzal(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToEternity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getCard().getId()));
        harness.passBothPriorities();
        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, returnedCreature.getId());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, existingAtzal.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Atzal, Cave of Eternity")).hasSize(1);
    }

    @Test
    void returnsBothWhenAuraAndCreatureAreDestroyedSimultaneouslyWithAuraFirst() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToEternity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Collections.swap(gd.playerBattlefields.get(player1.getId()), 0, 1);
        harness.addToBattlefield(player2, new NevinyrralsDisk());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Atzal, Cave of Eternity");
        harness.assertNotInGraveyard(player1, "Journey to Eternity");
    }

    private Permanent addTransformedAtzal(com.github.laxika.magicalvibes.model.Player player) {
        JourneyToEternity card = new JourneyToEternity();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }
}
