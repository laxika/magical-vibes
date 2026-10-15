package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralStatue.class, DragonScarredBear.class, Island.class})
class AncestralStatueTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only nonland permanents you control, including itself")
    void etbOffersControlledNonlandPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());

        castAncestralStatue();

        UUID statueId = harness.getPermanentId(player1, "Ancestral Statue");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(statueId, creature.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("The chosen nonland permanent returns to its owner's hand")
    void chosenPermanentReturnsToHand() {
        harness.addToBattlefield(player1, new DragonScarredBear());

        castAncestralStatue();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Dragon-Scarred Bear"));

        harness.assertInHand(player1, "Dragon-Scarred Bear");
        harness.assertOnBattlefield(player1, "Ancestral Statue");
    }

    @Test
    @DisplayName("It can return itself when it is the only nonland permanent you control")
    void canReturnItself() {
        castAncestralStatue();

        UUID statueId = harness.getPermanentId(player1, "Ancestral Statue");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(statueId);

        harness.handlePermanentChosen(player1, statueId);

        harness.assertInHand(player1, "Ancestral Statue");
        harness.assertNotOnBattlefield(player1, "Ancestral Statue");
    }

    @Test
    @DisplayName("A controlled permanent owned by the opponent returns to the opponent's hand")
    void returnsControlledPermanentToItsOwner() {
        DragonScarredBear bear = new DragonScarredBear();
        bear.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, bear);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control effect", null,
                player1.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT), stolen.getId(),
                null, null, EffectDuration.PERMANENT, 0));

        castAncestralStatue();
        harness.handlePermanentChosen(player1, stolen.getId());

        harness.assertInHand(player2, "Dragon-Scarred Bear");
        harness.assertNotInHand(player1, "Dragon-Scarred Bear");
        harness.assertNotOnBattlefield(player1, "Dragon-Scarred Bear");
        harness.assertOnBattlefield(player1, "Ancestral Statue");
    }

    @Test
    @DisplayName("An existing artifact can be returned while the entering Statue remains")
    void canReturnAnotherArtifact() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new AncestralStatue());

        castAncestralStatue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(existing.getId()).hasSize(2);
        harness.handlePermanentChosen(player1, existing.getId());

        harness.assertInHand(player1, "Ancestral Statue");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1).noneMatch(permanent -> permanent.getId().equals(existing.getId()));
    }

    private void castAncestralStatue() {
        harness.setHand(player1, List.of(new AncestralStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
