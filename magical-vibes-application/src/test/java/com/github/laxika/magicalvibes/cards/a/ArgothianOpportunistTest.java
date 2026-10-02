package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianOpportunist.class, EnergyRefractor.class})
class ArgothianOpportunistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a tapped Powerstone token")
    void etbCreatesTappedPowerstone() {
        harness.setHand(player1, List.of(new ArgothianOpportunist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Powerstone is created when the enters trigger resolves, even if its source has left")
    void triggerResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new ArgothianOpportunist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Argothian Opportunist");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();

        Permanent source = findPermanent(player1, "Argothian Opportunist");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player1, "Powerstone").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Powerstone cannot activate, but after untapping it produces one restricted mana immediately")
    void powerstoneProducesRestrictedManaAfterUntapping() {
        createPowerstone();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Powerstone"));
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.performUntapStep(player1);
        harness.activateAbility(player1, index, null, null);

        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Powerstone mana cannot pay the generic part of a nonartifact creature spell")
    void powerstoneCannotPayForNonartifactSpell() {
        createPowerstone();
        harness.performUntapStep(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Powerstone"));
        harness.activateAbility(player1, index, null, null);
        harness.setHand(player1, List.of(new ArgothianOpportunist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Argothian Opportunist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Powerstone mana can pay for an artifact spell")
    void powerstoneCanPayForArtifactSpell() {
        createPowerstone();
        harness.performUntapStep(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Powerstone"));
        harness.activateAbility(player1, index, null, null);
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Energy Refractor");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    private void createPowerstone() {
        harness.setHand(player1, List.of(new ArgothianOpportunist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
