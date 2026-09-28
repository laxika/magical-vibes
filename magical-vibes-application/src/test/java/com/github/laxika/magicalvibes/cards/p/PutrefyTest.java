package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Putrefy.class, Watchwolf.class, BorosSignet.class, Mountain.class})
class PutrefyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Putrefy destroys a target creature and it can't be regenerated")
    void destroysTargetCreatureIgnoringRegeneration() {
        Permanent watchwolf = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        watchwolf.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, watchwolf.getId());

        harness.assertNotOnBattlefield(player2, "Watchwolf");
        harness.assertInGraveyard(player2, "Watchwolf");
    }

    @Test
    @DisplayName("Resolving Putrefy destroys a target artifact")
    void destroysTargetArtifact() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, signet.getId());

        harness.assertNotOnBattlefield(player2, "Boros Signet");
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    @Test
    @DisplayName("Putrefy cannot target a land")
    void cannotTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
