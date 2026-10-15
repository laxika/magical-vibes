package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoothaMercurialArtist.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        HeatedDebate.class, Fireball.class})
class RoothaMercurialArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to hand and copies an instant or sorcery spell you control")
    void returnsToHandAndCopiesOwnSpell() {
        addReadyRootha(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, null, counsel.getId());

        harness.assertInHand(player1, "Rootha, Mercurial Artist");
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot copy an instant or sorcery spell controlled by another player")
    void cannotCopyOpponentSpell() {
        addReadyRootha(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot copy a creature spell")
    void cannotCopyCreatureSpell() {
        addReadyRootha(player1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileSummoningSickAndRetargetInstantCopy() {
        harness.addToBattlefield(player1, new RoothaMercurialArtist());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new RoothaMercurialArtist());
        HeatedDebate debate = new HeatedDebate();
        harness.setHand(player1, List.of(debate));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.activateAbility(player1, 0, null, debate.getId());
        harness.assertInHand(player1, "Rootha, Mercurial Artist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstTarget).doesNotContain(secondTarget);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstTarget.getCard(), secondTarget.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningRetargetKeepsOriginalTarget() {
        harness.addToBattlefield(player1, new RoothaMercurialArtist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HeatedDebate debate = new HeatedDebate();
        harness.setHand(player1, List.of(debate));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player1, 0, null, debate.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(debate);
    }

    @Test
    void canChooseNewTargetsForEveryTargetOfCopy() {
        harness.addToBattlefield(player1, new RoothaMercurialArtist());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Fireball fireball = new Fireball();
        harness.setHand(player1, List.of(fireball));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 4, List.of(player1.getId(), player2.getId()));
        harness.activateAbility(player1, 0, null, fireball.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstTarget.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstTarget.getCard(), secondTarget.getCard());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    private void addReadyRootha(Player player) {
        harness.addToBattlefieldAndReturn(player, new RoothaMercurialArtist()).setSummoningSick(false);
    }
}
