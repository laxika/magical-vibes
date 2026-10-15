package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HullbreakerHorror;
import com.github.laxika.magicalvibes.cards.l.LanternBearer;
import com.github.laxika.magicalvibes.cards.w.WeddingInvitation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyphonEssence.class, LanternBearer.class, SorinTheMirthless.class, WeddingInvitation.class, HullbreakerHorror.class})
class SyphonEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and creates a Blood token")
    void countersCreatureSpellAndCreatesBlood() {
        LanternBearer creature = new LanternBearer();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Lantern Bearer");
        harness.assertNotOnBattlefield(player1, "Lantern Bearer");
        assertThat(countPermanents(player2, "Blood")).isOne();
    }

    @Test
    @DisplayName("Counters a planeswalker spell and creates a Blood token")
    void countersPlaneswalkerSpellAndCreatesBlood() {
        SorinTheMirthless planeswalker = new SorinTheMirthless();
        harness.setHand(player1, List.of(planeswalker));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, planeswalker.getId());

        harness.assertInGraveyard(player1, "Sorin the Mirthless");
        harness.assertNotOnBattlefield(player1, "Sorin the Mirthless");
        assertThat(countPermanents(player2, "Blood")).isOne();
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker spell")
    void cannotTargetNoncreatureNonplaneswalkerSpell() {
        WeddingInvitation artifact = new WeddingInvitation();
        harness.setHand(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create Blood when its only target has left the stack")
    void doesNotCreateBloodWhenTargetLeavesStack() {
        LanternBearer creature = new LanternBearer();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new SyphonEssence(), new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(countPermanents(player2, "Blood")).isOne();

        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Blood")).isOne();
        harness.assertInGraveyard(player1, "Lantern Bearer");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof SyphonEssence).hasSize(2);
    }

    @Test
    @DisplayName("Can counter your own creature spell")
    void canCounterOwnCreatureSpell() {
        LanternBearer creature = new LanternBearer();
        harness.setHand(player1, List.of(creature, new SyphonEssence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Lantern Bearer");
        harness.assertNotOnBattlefield(player1, "Lantern Bearer");
        assertThat(countPermanents(player1, "Blood")).isOne();
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    @DisplayName("Created Blood token discards and sacrifices to draw a card")
    void createdBloodTokenDiscardsAndSacrificesToDraw() {
        LanternBearer creature = new LanternBearer();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        var blood = findPermanent(player2, "Blood");
        WeddingInvitation discarded = new WeddingInvitation();
        LanternBearer drawn = new LanternBearer();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blood);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Creates Blood even when the targeted creature spell cannot be countered")
    void createsBloodWhenTargetCannotBeCountered() {
        HullbreakerHorror creature = new HullbreakerHorror();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new SyphonEssence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(countPermanents(player2, "Blood")).isOne();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Hullbreaker Horror");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hullbreaker Horror");
    }
}
