package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForbiddenOrchard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSeism.class, Forest.class, Mountain.class, WanderingOnes.class, ForbiddenOrchard.class})
class ManaSeismTest extends BaseCardTest {

    @Test
    @DisplayName("Only lands the controller controls can be sacrificed")
    void promptsSacrificeChoiceForLandsOnly() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.addToBattlefieldAndReturn(player2, new Forest());
        castManaSeism();

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(forest.getId(), mountain.getId());
    }

    @Test
    @DisplayName("Nonbasic lands are also eligible for sacrifice")
    void acceptsNonbasicLands() {
        Permanent orchard = harness.addToBattlefieldAndReturn(player1, new ForbiddenOrchard());
        harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        castManaSeism();

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(orchard.getId());
    }

    @Test
    @DisplayName("Adds one colorless mana for each land sacrificed")
    void addsColorlessManaPerLandSacrificed() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        castManaSeism();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), mountain.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing no lands adds no mana and keeps every land")
    void sacrificeNoneAddsNoMana() {
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Mountain());
        castManaSeism();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no lands, the spell resolves with no prompt")
    void noLandsNoPrompt() {
        harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        castManaSeism();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void castManaSeism() {
        harness.castFromHand(player1, new ManaSeism(), "{1}{R}");
    }
}
