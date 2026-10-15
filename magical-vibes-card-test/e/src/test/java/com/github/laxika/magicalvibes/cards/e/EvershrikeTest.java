package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Evershrike")
@CardUsed({Evershrike.class, HolyStrength.class, Pacifism.class, FertileGround.class})
class EvershrikeTest extends BaseCardTest {

    private Permanent evershrikeOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Evershrike"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Graveyard ability returns Evershrike and attaches a chosen Aura within X")
    void returnsAndAttachesAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new HolyStrength())); // mana value 1
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1); // X = 1
        harness.passBothPriorities(); // return to battlefield, then prompt Aura choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent evershrike1 = evershrikeOnBattlefield();
        assertThat(evershrike1).isNotNull();

        Permanent aura = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Holy Strength"));
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(evershrike1.getId());
        harness.assertNotInGraveyard(player1, "Evershrike");
    }

    @Test
    @DisplayName("Evershrike gets +2/+2 for each Aura attached to it")
    void staticBoostPerAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent evershrike1 = evershrikeOnBattlefield();
        // Base 2/2 + static +2/+2 (one Aura) + Holy Strength +1/+2 = 5/6
        assertThat(harness.getGameQueryService().getEffectivePower(gd, evershrike1)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, evershrike1)).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining the Aura exiles Evershrike")
    void decliningExilesEvershrike() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);

        harness.handleCardChosen(player1, -1); // decline

        assertThat(evershrikeOnBattlefield()).isNull();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Evershrike"));
    }

    @Test
    @DisplayName("Evershrike is exiled when no Aura with mana value X or less is in hand")
    void exiledWhenNoEligibleAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new Pacifism())); // mana value 2 > X (1)
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetedHandCardChoice.class)).isNull();
        assertThat(evershrikeOnBattlefield()).isNull();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Evershrike"));
        harness.assertInHand(player1, "Pacifism");
    }

    @Test
    @DisplayName("A higher X allows a costlier Aura to be attached")
    void higherXAllowsCostlierAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new Pacifism())); // mana value 2
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, 0, 2); // X = 2
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent evershrike1 = evershrikeOnBattlefield();
        assertThat(evershrike1).isNotNull();
        Permanent aura = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Pacifism"));
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(evershrike1.getId());
    }

    @Test
    @DisplayName("An Aura that enchants land cannot save Evershrike")
    void cannotAttachLandAura() {
        harness.setGraveyard(player1, List.of(new Evershrike()));
        harness.setHand(player1, List.of(new FertileGround()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.TargetedHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.TargetedHandCardChoice.class);
        if (choice != null) {
            assertThat(choice.validIndices()).doesNotContain(0);
            harness.handleCardChosen(player1, -1);
        }
        harness.assertInHand(player1, "Fertile Ground");
        harness.assertNotOnBattlefield(player1, "Evershrike");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Evershrike"));
    }

    @Test
    @DisplayName("An earlier activation cannot exile the Evershrike returned by a later activation")
    void earlierActivationCannotAffectReturnedCreature() {
        harness.setGraveyard(player1, List.of(new Evershrike()));
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, 0, 1);
        harness.activateGraveyardAbility(player1, 0, 0, 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Evershrike");
        harness.assertOnBattlefield(player1, "Holy Strength");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Evershrike"));
    }

    @Test
    @DisplayName("X zero is legal and exiles Evershrike when the only Aura costs one")
    void zeroXExilesWithoutEligibleAura() {
        harness.setGraveyard(player1, List.of(new Evershrike()));
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0, 0, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Evershrike");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Evershrike"));
    }

    @Test
    @DisplayName("Each attached Aura counts, including an opponent's Aura, but unrelated Auras do not")
    void multipleAurasCountRegardlessOfController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Evershrike());
        Permanent strength = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        strength.setAttachedTo(creature.getId());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        pacifism.setAttachedTo(creature.getId());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new Evershrike());
        Permanent unrelated = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        unrelated.setAttachedTo(other.getId());

        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, creature)).isEqualTo(8);
    }
}
