package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthKingdomJailer.class, GloriousAnthem.class, GrizzlyBears.class, HillGiant.class,
        LeoninScimitar.class, LightningBolt.class, LoxodonWarhammer.class})
class EarthKingdomJailerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent's creature with mana value 3 or greater")
    void etbExilesTargetCreatureWithMinimumManaValue() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("ETB can exile an opponent's artifact")
    void etbExilesTargetArtifact() {
        harness.addToBattlefield(player2, new LoxodonWarhammer());
        UUID targetId = harness.getPermanentId(player2, "Loxodon Warhammer");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Loxodon Warhammer");
    }

    @Test
    @DisplayName("ETB can exile an opponent's enchantment")
    void etbExilesTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Exiled permanent returns when Earth Kingdom Jailer leaves")
    void exiledPermanentReturnsWhenJailerLeaves() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castAndResolve(targetId);
        destroyJailer();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Hill Giant"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("ETB does not target permanents with mana value below 3")
    void etbSkipsPermanentsWithLowManaValue() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EarthKingdomJailer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Earth Kingdom Jailer");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the jailer's controller")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID ownPermanentId = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(new EarthKingdomJailer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolve(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EarthKingdomJailer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyJailer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID jailerId = harness.getPermanentId(player1, "Earth Kingdom Jailer");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, jailerId);
        harness.passBothPriorities();
    }
}
