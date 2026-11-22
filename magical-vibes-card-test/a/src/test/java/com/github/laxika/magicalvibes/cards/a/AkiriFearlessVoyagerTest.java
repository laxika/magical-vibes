package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.b.BludgeonBrawl;
import com.github.laxika.magicalvibes.cards.t.TitanForge;
import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.effect.CantBeChosenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkiriFearlessVoyager.class, GrizzlyBears.class, LeoninScimitar.class, BludgeonBrawl.class, TitanForge.class})
class AkiriFearlessVoyagerTest extends BaseCardTest {

    @Test
    @DisplayName("Akiri draws a card when an equipped creature attacks a player")
    void drawsWhenEquippedCreatureAttacksPlayer() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize + 1);
    }

    @Test
    @DisplayName("Akiri does not draw when no attacking creature is equipped")
    void doesNotDrawWhenNoAttackingCreatureIsEquipped() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize);
    }

    @Test
    @DisplayName("Akiri unattaches the chosen Equipment and protects its creature")
    void unattachesChosenEquipmentAndGrantsIndestructible() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(scimitar.getId()));

        assertThat(scimitar.getAttachedTo()).isNull();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Akiri's optional ability can be declined")
    void canDeclineUnattachingEquipment() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void drawsOnlyOnceForMultipleEquippedAttackers() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new LeoninScimitar()).setAttachedTo(first.getId());
        harness.addToBattlefieldAndReturn(player1, new LeoninScimitar()).setAttachedTo(second.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize + 1);
    }

    @Test
    void equippedNonattackerDoesNotCauseDraw() {
        Permanent akiri = addCreatureReady(player1, new AkiriFearlessVoyager());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new LeoninScimitar()).setAttachedTo(akiri.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize);
    }

    @Test
    void alreadyTappedCreatureGainsIndestructibleAndKeepsOtherEquipment() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        chosen.setAttachedTo(creature.getId());
        remaining.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen);
        assertThat(remaining.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canUnattachOpponentsEquipmentFromOwnCreature() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void equipmentOnOpponentsCreatureCannotBeUnattached() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Akiri doesn't offer an Equipment that can't be chosen")
    void doesNotOfferEquipmentThatCantBeChosen() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent unchoosable = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        unchoosable.setAttachedTo(creature.getId());
        TestCards.mutableCard(unchoosable).addStaticEffect(new CantBeChosenEffect());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(unchoosable.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Akiri can unattach an artifact that became an Equipment")
    void unattachesArtifactThatBecameEquipment() {
        addCreatureReady(player1, new AkiriFearlessVoyager());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TitanForge());
        forge.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(forge.getId()));

        assertThat(forge.getAttachedTo()).isNull();
        assertThat(creature.isTapped()).isTrue();
    }
}
