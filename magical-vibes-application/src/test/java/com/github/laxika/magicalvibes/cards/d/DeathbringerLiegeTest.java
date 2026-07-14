package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeathbringerLiegeTest extends BaseCardTest {

    // ===== Static effects: +1/+1 to own white / black creatures =====

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player1, new SuntailHawk());

        Permanent hawk = findPermanent(player1, "Suntail Hawk");
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other black creatures you control get +1/+1")
    void buffsOwnBlackCreatures() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        Permanent ghoul = findPermanent(player1, "Diregraf Ghoul");
        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-white non-black creatures")
    void doesNotBuffOffColorCreatures() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== White spell cast trigger: tap target creature =====

    @Test
    @DisplayName("Casting a white spell offers the tap-target may ability")
    void whiteSpellTriggersTap() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A black spell does not fire the white tap trigger")
    void blackSpellDoesNotFireWhiteTrigger() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        // Black spell fires only the destroy trigger; declining leaves the untapped bear alone.
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Black spell cast trigger: destroy target creature if tapped =====

    @Test
    @DisplayName("Casting a black spell destroys a tapped target creature")
    void blackSpellDestroysTappedCreature() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        bears.tap();

        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An untapped target survives the black spell trigger")
    void blackSpellSparesUntappedCreature() {
        harness.addToBattlefield(player1, new DeathbringerLiege());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DiregrafGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
