package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.h.HuntTheWeak;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildRicochet.class, LavaAxe.class, GrizzlyBears.class, Divination.class, Shock.class, HuntTheWeak.class})
class WildRicochetTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Wild Ricochet puts it on the stack targeting an instant or sorcery spell")
    void castingPutsOnStackTargetingSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry ricochetEntry = gd.stack.getLast();
        assertThat(ricochetEntry.getCard().getName()).isEqualTo("Wild Ricochet");
        assertThat(ricochetEntry.getTargetId()).isEqualTo(lavaAxe.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell with Wild Ricochet")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving offers the may-ability to choose new targets for the original spell")
    void resolvingOffersRetargetOriginalPrompt() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the original retarget still copies the spell, and the copy inherits the original target")
    void decliningOriginalRetargetStillCopies() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        // Lava Axe targets player2 (the Wild Ricochet caster)
        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());
        // Decline retargeting the original
        harness.handleMayAbilityChosen(player2, false);

        GameData gd = harness.getGameData();
        // A copy of Lava Axe was created; a copy-retarget may prompt is now offered
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Lava Axe");
        assertThat(copyEntry.getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Retargeting the original redirects both the original and its copy")
    void retargetOriginalRedirectsBoth() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Lava Axe originally targets player2 (the Wild Ricochet caster)
        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());

        // The copy is now created inheriting the retargeted (player1) target; decline retargeting it
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        // Resolve copy, then original — both hit player1
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Copy can be given a different target than the original")
    void copyCanBeRetargetedSeparately() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Lava Axe originally targets player1
        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());
        harness.handleMayAbilityChosen(player2, false);

        // Accept retargeting the copy to player2
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player2.getId());

        // Resolve copy (hits player2), then original (hits player1)
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 5);
    }

    // ===== Copy does not persist =====

    @Test
    @DisplayName("Wild Ricochet goes to its caster's graveyard and the copy does not go to any graveyard")
    void ricochetToGraveyardCopyDoesNot() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        // Resolve copy, then original
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Wild Ricochet");
        harness.assertNotInGraveyard(player2, "Lava Axe");
        // The original Lava Axe belongs to player1's graveyard
        harness.assertInGraveyard(player1, "Lava Axe");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untargetedSpellCopyDrawsForRicochetController() {
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(new LavaAxe(), new LavaAxe()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(divination));
        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, divination.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Divination");
    }

    @Test
    void instantCopyResolvesBeforeOriginalWithIndependentTarget() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void originalCanChangeSecondTargetWhenFirstHasNoAlternative() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var ownBear = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        var firstOpponentBear = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        var secondOpponentBear = gd.playerBattlefields.get(player2.getId()).getLast().getId();
        HuntTheWeak hunt = new HuntTheWeak();
        harness.setHand(player1, List.of(hunt));
        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(ownBear, firstOpponentBear));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunt.getId());
        harness.handleMayAbilityChosen(player2, true);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }
        harness.handlePermanentChosen(player2, secondOpponentBear);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(ownBear, secondOpponentBear);
    }

    @Test
    void copyCanChangeBothTargetsUsingItsOwnControllerRestrictions() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var firstBear = harness.getPermanentId(player1, "Grizzly Bears");
        var secondBear = harness.getPermanentId(player2, "Grizzly Bears");
        HuntTheWeak hunt = new HuntTheWeak();
        harness.setHand(player1, List.of(hunt));
        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(firstBear, secondBear));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunt.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, secondBear);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }
        harness.handlePermanentChosen(player2, firstBear);
        assertThat(gd.stack.getLast().getTargetIds()).containsExactly(secondBear, firstBear);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(firstBear, secondBear);
    }
}
