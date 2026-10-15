package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuciusTheEternal.class, GrizzlyBears.class, Murder.class})
class LuciusTheEternalTest extends BaseCardTest {

    @Test
    @DisplayName("When Lucius dies, it exiles itself haunting an opponent's creature and returns when it leaves")
    void hauntsOpponentCreatureAndReturnsWhenItLeaves() {
        Permanent lucius = harness.addToBattlefieldAndReturn(player1, new LuciusTheEternal());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithMurder(player2, lucius.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lucius);
        assertThat(gd.hauntingCardToPermanentId).containsEntry(lucius.getCard().getId(), bears.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(lucius.getCard().getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, bears));

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(lucius.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(lucius.getCard().getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(lucius.getCard().getId()));
        assertThat(gd.hauntingCardToPermanentId).doesNotContainKey(lucius.getCard().getId());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(lucius.getCard().getId()));
    }

    @Test
    @DisplayName("Lucius's death trigger is skipped when the opponent controls no creature")
    void deathTriggerNeedsAnOpponentCreature() {
        Permanent lucius = harness.addToBattlefieldAndReturn(player1, new LuciusTheEternal());

        destroyWithMurder(player2, lucius.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lucius.getCard());
    }

    @Test
    @DisplayName("A creature controlled by Lucius's controller cannot keep the death trigger on the stack")
    void ownCreatureIsNotALegalTarget() {
        Permanent lucius = harness.addToBattlefieldAndReturn(player1, new LuciusTheEternal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyWithMurder(player2, lucius.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lucius.getCard());
    }

    @Test
    @DisplayName("Lucius stays in the graveyard if its target leaves before the death trigger resolves")
    void targetLeavingBeforeResolutionPreventsExile() {
        Permanent lucius = harness.addToBattlefieldAndReturn(player1, new LuciusTheEternal());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWithMurder(player2, lucius.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lucius.getCard());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(lucius.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The return ability cannot find Lucius after it leaves exile and is exiled again")
    void leavingAndReenteringExileBreaksTheReturnLink() {
        Permanent lucius = harness.addToBattlefieldAndReturn(player1, new LuciusTheEternal());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWithMurder(player2, lucius.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            assertThat(gd.removeFromExile(lucius.getCard().getId())).isTrue();
            gd.addCardToHand(player1.getId(), lucius.getCard());
            gd.playerHands.get(player1.getId()).remove(lucius.getCard());
            gd.addToExile(player1.getId(), lucius.getCard());
        });
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(lucius.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(lucius.getCard().getId()));
    }

    private void destroyWithMurder(Player caster, java.util.UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
