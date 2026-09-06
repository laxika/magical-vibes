package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WoodElemental.class, Forest.class})
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
