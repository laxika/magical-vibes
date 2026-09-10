package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelEdict.class, AlabornTrooper.class, BearCub.class, Plains.class})
class CruelEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cruel Edict targeting opponent puts it on the stack")
    void castingPutsOnStack() {
        CruelEdict edict = new CruelEdict();
        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(edict);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it automatically")
    void opponentWithOneCreatureSacrificesAutomatically() {
        Permanent bearCub = harness.addToBattlefieldAndReturn(player2, new BearCub());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bearCub);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bearCub.getCard());
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose")
    void opponentWithMultipleCreaturesChooses() {
        Permanent bearCub = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent trooper = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(bearCub.getId(), trooper.getId());
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent bearCub = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent trooper = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, bearCub.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bearCub).contains(trooper);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bearCub.getCard());
    }

    @Test
    @DisplayName("Noncreature permanents are not eligible for sacrifice")
    void noncreaturePermanentsAreNotSacrificed() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent bearCub = harness.addToBattlefieldAndReturn(player2, new BearCub());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains).doesNotContain(bearCub);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bearCub.getCard());
    }

    @Test
    @DisplayName("No effect when opponent has no creatures")
    void noEffectWhenOpponentHasNoCreatures() {
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("no creatures to sacrifice")).isTrue();
    }

    @Test
    @DisplayName("Cruel Edict goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefieldAndReturn(player2, new BearCub());
        CruelEdict edict = new CruelEdict();

        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(edict);
    }

    @Test
    @DisplayName("Cruel Edict cannot target its caster")
    void cannotTargetItsCaster() {
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

