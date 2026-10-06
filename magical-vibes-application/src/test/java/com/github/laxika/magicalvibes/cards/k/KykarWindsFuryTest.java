package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SpectralSailor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KykarWindsFury.class, GrizzlyBears.class, Spellbook.class, SpectralSailor.class})
class KykarWindsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a 1/1 white Spirit with flying")
    void noncreatureSpellCreatesFlyingSpirit() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Spirit")
    void creatureSpellDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Sacrificing a Spirit adds one red mana")
    void sacrificingSpiritAddsRedMana() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spirit");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability requires a Spirit to sacrifice")
    void requiresSpiritToSacrifice() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Spirit trigger resolves before the noncreature spell")
    void spiritIsCreatedBeforeSpellResolves() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spirit");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not create a Spirit")
    void opponentSpellDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.setHand(player2, List.of(new Spellbook()));
        gd.activePlayerId = player2.getId();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped nontoken Spirit can be sacrificed without tapping Kykar")
    void sacrificesTappedNontokenSpirit() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.addToBattlefield(player1, new SpectralSailor());
        findPermanent(player1, "Kykar, Wind's Fury").tap();
        findPermanent(player1, "Spectral Sailor").tap();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spectral Sailor");
        harness.assertInGraveyard(player1, "Spectral Sailor");
        harness.assertOnBattlefield(player1, "Kykar, Wind's Fury");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Spirit cannot pay Kykar's sacrifice cost")
    void cannotSacrificeOpponentsSpirit() {
        harness.addToBattlefield(player1, new KykarWindsFury());
        harness.addToBattlefield(player2, new SpectralSailor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Spectral Sailor");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
