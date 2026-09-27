package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodElemental.class, Forest.class, DurkwoodBoars.class})
class WoodElementalTest extends BaseCardTest {

    private void castWoodElemental() {
        harness.setHand(player1, List.of(new WoodElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Power and toughness equal the number of untapped Forests sacrificed")
    void powerToughnessEqualSacrificedUntappedForests() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        tapped.tap();

        castWoodElemental();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        Permanent woodElemental = findPermanent(player1, "Wood Elemental");
        assertThat(gqs.getEffectivePower(gd, woodElemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, woodElemental)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tapped);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Forest");
    }

    @Test
    @DisplayName("Only its controller's untapped Forests can be sacrificed")
    void onlyControllerForestsCanBeSacrificed() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castWoodElemental();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(ownForest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownForest.getId()));

        Permanent woodElemental = findPermanent(player1, "Wood Elemental");
        assertThat(gqs.getEffectivePower(gd, woodElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodElemental)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentForest);
    }

    @Test
    @DisplayName("Untapped non-Forest permanents cannot be sacrificed")
    void untappedNonForestPermanentsCannotBeSacrificed() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nonForest = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());

        castWoodElemental();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(forest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonForest);
    }

    @Test
    @DisplayName("Sacrificing no Forests leaves it a 0/0 that dies")
    void sacrificingNoForestsDies() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        tapped.tap();

        castWoodElemental();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wood Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Wood Elemental"));
    }

    @Test
    @DisplayName("Choosing no Forests at the prompt also makes it die")
    void choosingNoForestsDies() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castWoodElemental();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wood Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Wood Elemental"));
    }
}
