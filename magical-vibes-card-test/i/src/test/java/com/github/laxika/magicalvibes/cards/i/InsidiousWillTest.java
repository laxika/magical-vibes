package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsidiousWill.class, CounselOfTheSoratami.class, GrizzlyBears.class, LavaAxe.class,
        ChandrasPyrohelix.class})
class InsidiousWillTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode counters any target spell")
    void counterModeCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Retarget mode can choose a new target for a spell")
    void retargetModeChoosesNewTarget() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, lavaAxe.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 5);
    }

    @Test
    @DisplayName("Copy mode copies an instant or sorcery spell")
    void copyModeCopiesInstantOrSorcery() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{2}, counsel.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Counsel of the Soratami");
        assertThat(copyEntry.isCopy()).isTrue();
    }

    @Test
    @DisplayName("Copy mode cannot target a creature spell")
    void copyModeRejectsCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player2, 0, 1, new int[]{2}, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retargetModeCanKeepOriginalTarget() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, lavaAxe.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void copiedSpellCanResolveWithNewTargetWithoutChangingOriginal() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{2}, lavaAxe.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }

    @Test
    void retargetedDividedDamageCopyDealsDamageToNewTarget() {
        ChandrasPyrohelix pyrohelix = new ChandrasPyrohelix();
        harness.setHand(player1, List.of(pyrohelix));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{2}, pyrohelix.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void copyModeAllowsChangingBothTargetsWhilePreservingDamageDivision() {
        ChandrasPyrohelix pyrohelix = new ChandrasPyrohelix();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(pyrohelix));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new InsidiousWill()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        assignments.put(first.getId(), 1);
        assignments.put(second.getId(), 1);

        harness.castInstant(player1, 0, assignments);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{2}, pyrohelix.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
    }
}
