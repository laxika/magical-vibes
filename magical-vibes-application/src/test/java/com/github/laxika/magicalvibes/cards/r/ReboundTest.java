package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LabRats;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rebound.class, Shock.class, SpinedWurm.class, LabRats.class, ManaLeak.class})
class ReboundTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rebound requires targeting a spell with a single player target")
    void castingRequiresSinglePlayerTargetSpell() {
        UUID wurmPermId = harness.addToBattlefieldAndReturn(player1, new SpinedWurm()).getId();

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wurmPermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Rebound()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player target");
    }

    @Test
    @DisplayName("Resolving Rebound retargets a player-target spell to another player")
    void resolvingRetargetsPlayerTargetSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID wurmPermId = harness.addToBattlefieldAndReturn(player1, new SpinedWurm()).getId();

        harness.setHand(player2, List.of(new Rebound()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId())
                .doesNotContain(player2.getId(), wurmPermId);

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, p1LifeBefore - 2);
        harness.assertLife(player2, p2LifeBefore);
    }

    @Test
    @DisplayName("Rebound cannot target a spell that affects a player without targeting")
    void rejectsSpellWithoutTargets() {
        LabRats rats = new LabRats();
        harness.castFromHand(player1, rats, "{B}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Rebound()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, rats.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player target");
    }

    @Test
    @DisplayName("Rebound cannot target a spell whose only target is another spell")
    void rejectsSpellTargetingAnotherSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        ManaLeak leak = new ManaLeak();
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Rebound()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, leak.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player target");
    }

    @Test
    @DisplayName("Rebound can redirect its controller's own spell")
    void redirectsOwnSpell() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new Rebound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0, shock.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Rebound");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Rebound does nothing when its targeted spell is countered in response")
    void targetedSpellCounteredInResponse() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Rebound(), new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, shock.getId());
        harness.castAndResolveInstant(player2, 0, shock.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Rebound");
        harness.assertInGraveyard(player2, "Mana Leak");
    }
}
