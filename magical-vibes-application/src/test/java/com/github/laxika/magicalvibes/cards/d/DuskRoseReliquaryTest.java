package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskRoseReliquary.class, Forest.class, GrizzlyBears.class, Naturalize.class, Spellbook.class})
class DuskRoseReliquaryTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and exiles an opponent's creature")
    void sacrificesArtifactAndExilesCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castReliquary(target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Dusk Rose Reliquary");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sacrifices a creature and exiles an opponent's artifact")
    void sacrificesCreatureAndExilesArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        castReliquary(target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Dusk Rose Reliquary leaves")
    void exiledPermanentReturnsWhenSourceLeaves() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castReliquary(target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID reliquaryId = harness.getPermanentId(player1, "Dusk Rose Reliquary");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, reliquaryId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dusk Rose Reliquary");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {2}")
    void wardCountersUnpaidSpell() {
        harness.addToBattlefield(player1, new DuskRoseReliquary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID reliquaryId = harness.getPermanentId(player1, "Dusk Rose Reliquary");
        harness.castInstant(player2, 0, reliquaryId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dusk Rose Reliquary");
        harness.assertInGraveyard(player2, "Naturalize");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castReliquary(target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castReliquary(UUID targetId, UUID sacrificeId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DuskRoseReliquary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false, sacrificeId);
    }
}
