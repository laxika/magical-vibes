package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KeenEyedArchers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SenuKeenEyedProtector.class, ShayCormac.class, GrizzlyBears.class, KeenEyedArchers.class, AjaniGoldmane.class})
class SenuKeenEyedProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and exiling Senu gains 2 life and scries 2")
    void activationGainsLifeAndScries() {
        Card bottomCard = new GrizzlyBears();
        Card topCard = new GrizzlyBears();
        Permanent senu = addCreatureReady(player1, new SenuKeenEyedProtector());
        harness.setLibrary(player1, List.of(bottomCard, topCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(senu), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(bottomCard, topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(senu.getCard());

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, bottomCard);
    }

    @Test
    @DisplayName("An unblocked legendary attacker returns Senu from exile attacking")
    void legendaryUnblockedAttackerReturnsSenuAttacking() {
        SenuKeenEyedProtector exiledSenu = new SenuKeenEyedProtector();
        exileSenu(exiledSenu);
        Permanent attacker = addCreatureReady(player1, new ShayCormac());

        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(exiledSenu.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledSenu);
    }

    @Test
    @DisplayName("A nonlegendary unblocked attacker does not return Senu from exile")
    void nonlegendaryUnblockedAttackerDoesNotReturnSenu() {
        SenuKeenEyedProtector exiledSenu = new SenuKeenEyedProtector();
        exileSenu(exiledSenu);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(exiledSenu.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledSenu);
    }

    @Test
    @DisplayName("A blocked legendary attacker does not return Senu from exile")
    void blockedLegendaryAttackerDoesNotReturnSenu() {
        SenuKeenEyedProtector exiledSenu = new SenuKeenEyedProtector();
        exileSenu(exiledSenu);
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent blocker = addCreatureReady(player2, new KeenEyedArchers());

        declareAttackersAndPrepareBlockers(List.of(battlefieldIndex(player1, attacker)));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker), battlefieldIndex(player1, attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(exiledSenu.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledSenu);
    }

    @Test
    @DisplayName("Senu can be activated without any mana")
    void activationRequiresNoMana() {
        Permanent senu = addCreatureReady(player1, new SenuKeenEyedProtector());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(senu), 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(senu);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(senu.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An opponent's unblocked legendary attacker does not return Senu")
    void opponentsLegendaryAttackerDoesNotReturnSenu() {
        SenuKeenEyedProtector exiledSenu = new SenuKeenEyedProtector();
        exileSenu(exiledSenu);
        Permanent attacker = addCreatureReady(player2, new ShayCormac());

        declareAttackers(player2, List.of(battlefieldIndex(player2, attacker)));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledSenu);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(exiledSenu.getId()));
    }

    @Test
    @DisplayName("Senu in the graveyard is not returned by an unblocked legendary attacker")
    void graveyardSenuDoesNotReturn() {
        SenuKeenEyedProtector senu = new SenuKeenEyedProtector();
        harness.setGraveyard(player1, List.of(senu));
        Permanent attacker = addCreatureReady(player1, new ShayCormac());

        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(senu);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(senu.getId()));
    }

    @Test
    @DisplayName("Senu may enter attacking a planeswalker instead of the unblocked attacker's target")
    void returnedSenuCanChooseDifferentAttackTarget() {
        SenuKeenEyedProtector exiledSenu = new SenuKeenEyedProtector();
        exileSenu(exiledSenu);
        Permanent attacker = addCreatureReady(player1, new ShayCormac());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new AjaniGoldmane());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(battlefieldIndex(attacker)));
            harness.passBothPriorities();
            resolveAllTriggers();

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).contains(player2.getId(), planeswalker.getId());
            harness.handlePermanentChosen(player1, planeswalker.getId());

            Permanent returned = findPermanent(player1, "Senu, Keen-Eyed Protector");
            assertThat(returned.getAttackTarget()).isEqualTo(planeswalker.getId());
            assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledSenu);
        });
    }

    private void exileSenu(SenuKeenEyedProtector senu) {
        harness.setExile(player1, List.of(senu));
    }

    private int battlefieldIndex(Permanent permanent) {
        return battlefieldIndex(player1, permanent);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
