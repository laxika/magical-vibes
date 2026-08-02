package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SnapcasterMage;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A source that taps for one mana of any colour (Birds of Paradise) must let the MTGO-style
 * click-to-cast highlight offer spells with a coloured pip of any colour, while still counting as
 * the single mana one tap actually produces.
 */
class AnyColorManaSourcePlayabilityTest extends BaseCardTest {

    private GameActionAvailabilityService availability() {
        return harness.getGameActionAvailabilityService();
    }

    private List<Integer> potentiallyPlayable() {
        GameActionAvailabilityService svc = availability();
        return svc.getPotentialPlayableCardIndices(gd, player1.getId(),
                svc.getPlayableCardIndices(gd, player1.getId()));
    }

    private void addReadyBirds() {
        Permanent birds = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
        birds.setSummoningSick(false);
    }

    @Test
    @DisplayName("Birds of Paradise pays the {G} pip of an aura the Island cannot")
    void anyColorSourceCoversAColoredPipNoOtherSourceCan() {
        addReadyBirds();
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WildGrowth(), new SnapcasterMage()));

        assertThat(potentiallyPlayable())
                .as("Wild Growth {G} off the Birds, Snapcaster Mage {1}{U} off Island + Birds")
                .containsExactly(0, 1);
    }

    @Test
    @DisplayName("A lone Birds of Paradise makes a {G} spell castable")
    void anyColorSourceAlonePaysAColoredPip() {
        addReadyBirds();
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WildGrowth()));

        assertThat(potentiallyPlayable()).containsExactly(0);
    }

    @Test
    @DisplayName("A lone Birds of Paradise is one mana, not one per colour")
    void anyColorSourceCountsAsASingleMana() {
        addReadyBirds();
        harness.setHand(player1, List.of(new SnapcasterMage()));

        assertThat(availability().getPotentialManaTotal(gd, player1.getId())).isEqualTo(1);
        assertThat(potentiallyPlayable())
                .as("Snapcaster Mage costs {1}{U}; one Birds cannot pay two mana")
                .isEmpty();
    }
}
