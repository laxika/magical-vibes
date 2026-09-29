package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelEdict.class, GiantSpider.class, GrizzlyBears.class, Plains.class})
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
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(grizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(grizzlyBears.getCard());
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose")
    void opponentWithMultipleCreaturesChooses() {
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giantSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(grizzlyBears.getId(), giantSpider.getId());
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giantSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handlePermanentChosen(player2, grizzlyBears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(grizzlyBears).contains(giantSpider);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(grizzlyBears.getCard());
    }

    @Test
    @DisplayName("Noncreature permanents are not eligible for sacrifice")
    void noncreaturePermanentsAreNotSacrificed() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains).doesNotContain(grizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(grizzlyBears.getCard());
    }

    @Test
    @DisplayName("No effect when opponent has no creatures")
    void noEffectWhenOpponentHasNoCreatures() {
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("no creatures to sacrifice")).isTrue();
    }

    @Test
    @DisplayName("Cruel Edict goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        CruelEdict edict = new CruelEdict();

        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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

