package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.a.AuriokBladewarden;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        SecondSunrise.class,
        AuriokBladewarden.class,
        Bonesplitter.class,
        SphereOfPurity.class,
        AncientDen.class,
        Annul.class,
        Arrest.class
})
class SecondSunriseTest extends BaseCardTest {

    private void castSecondSunrise() {
        harness.castFromHand(player1, new SecondSunrise(), "{1}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player returns artifact, creature, enchantment, and land cards put there from the battlefield this turn")
    void returnsQualifyingCardsForEachPlayer() {
        Permanent p1Creature = harness.addToBattlefieldAndReturn(player1, new AuriokBladewarden());
        Permanent p1Artifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent p1Enchantment = harness.addToBattlefieldAndReturn(player1, new SphereOfPurity());
        Permanent p1Land = harness.addToBattlefieldAndReturn(player1, new AncientDen());
        Permanent p2Creature = harness.addToBattlefieldAndReturn(player2, new AuriokBladewarden());
        Permanent p2Land = harness.addToBattlefieldAndReturn(player2, new AncientDen());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Artifact);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Enchantment);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Land);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p2Creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p2Land);
        });

        castSecondSunrise();

        harness.assertOnBattlefield(player1, "Auriok Bladewarden");
        harness.assertOnBattlefield(player1, "Bonesplitter");
        harness.assertOnBattlefield(player1, "Sphere of Purity");
        harness.assertOnBattlefield(player1, "Ancient Den");
        harness.assertOnBattlefield(player2, "Auriok Bladewarden");
        harness.assertOnBattlefield(player2, "Ancient Den");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not return cards that were not put into the graveyard from the battlefield this turn")
    void ignoresOldCardsAndNonpermanents() {
        Card oldCreature = new AuriokBladewarden();
        Card instant = new Annul();
        Card p2OldCreature = new AuriokBladewarden();
        harness.setGraveyard(player1, List.of(oldCreature, instant));
        harness.setGraveyard(player2, List.of(p2OldCreature));

        castSecondSunrise();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(oldCreature.getId(), instant.getId());
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(p2OldCreature.getId());
        harness.assertNotOnBattlefield(player1, "Auriok Bladewarden");
    }

    @Test
    void choosesAnExistingCreatureForReturningAura() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AuriokBladewarden());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AuriokBladewarden());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Arrest());
        aura.setAttachedTo(first.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        castSecondSunrise();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(findPermanent(player1, "Arrest").getAttachedTo()).isEqualTo(second.getId());
        harness.assertNotInGraveyard(player1, "Arrest");
    }

    @Test
    void leavesAuraInGraveyardWhenOnlyCreatureIsReturningSimultaneously() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AuriokBladewarden());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Arrest());
        aura.setAttachedTo(creature.getId());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removeOrphanedAuras(gd);
        });

        castSecondSunrise();

        harness.assertOnBattlefield(player1, "Auriok Bladewarden");
        harness.assertNotOnBattlefield(player1, "Arrest");
        harness.assertInGraveyard(player1, "Arrest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canReturnTheSameCardAfterItDiesAgainThisTurn() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new AuriokBladewarden());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, original));
        castSecondSunrise();
        Permanent returned = findPermanent(player1, "Auriok Bladewarden");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returned));

        castSecondSunrise();

        assertThat(findPermanent(player1, "Auriok Bladewarden").getId()).isNotEqualTo(returned.getId());
        assertThat(countPermanents(player1, "Auriok Bladewarden")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Auriok Bladewarden");
    }

    @Test
    void resolvesWithoutEligibleCards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castSecondSunrise();

        harness.assertInGraveyard(player1, "Second Sunrise");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
