package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnToTheSewers.class, GrizzlyBears.class, Island.class, DryadArbor.class})
class ReturnToTheSewersTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can put it on top and the spell creates a Mutagen")
    void putsTargetOnTopAndCreatesMutagen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), existingTop);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findMutagen(player1)).isNotNull();
    }

    @Test
    @DisplayName("The target's owner can put it on the bottom")
    void putsTargetOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Mutagen can be sacrificed to put a +1/+1 counter on a creature")
    void mutagenPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);
        harness.handleListChoice(player2, "Top");

        Permanent mutagen = findMutagen(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0,
                null,
                recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getEffectivePower()).isEqualTo(3);
        assertThat(findMutagen(player1)).isNull();
    }

    @Test
    @DisplayName("Return to the Sewers cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ReturnToTheSewers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The owner chooses the destination even when another player controls the creature")
    void ownerChoosesForStolenCreature() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, card);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findMutagen(player1)).isNotNull();
        assertThat(findMutagen(player2)).isNull();
    }

    @Test
    @DisplayName("No Mutagen is created if the only target leaves before resolution")
    void illegalTargetPreventsMutagenCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReturnToTheSewers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(findMutagen(player1)).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mutagen cannot be activated outside a main phase")
    void mutagenRequiresSorceryTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);
        harness.handleListChoice(player2, "Top");
        Permanent mutagen = findMutagen(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0, null, recipient.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findMutagen(player1)).isSameAs(mutagen);
        assertThat(recipient.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mutagen can put a counter on an opponent's creature")
    void mutagenCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);
        harness.handleListChoice(player2, "Bottom");
        Permanent mutagen = findMutagen(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0, null, recipient.getId());
        assertThat(findMutagen(player1)).isNull();
        harness.passBothPriorities();

        assertThat(recipient.getEffectivePower()).isEqualTo(3);
        assertThat(recipient.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Land creatures are legal targets")
    void canPutLandCreatureIntoLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        cast(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, target.getCard());
        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
        assertThat(findMutagen(player1)).isNotNull();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ReturnToTheSewers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent findMutagen(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Mutagen"))
                .findFirst()
                .orElse(null);
    }
}
