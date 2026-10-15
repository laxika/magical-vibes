package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.u.UnderdarkBasilisk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RogueClass.class, UnderdarkBasilisk.class, Forest.class})
class RogueClassTest extends BaseCardTest {

    @Test
    void levelTwoGivesMenaceToCreaturesYouControl() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnderdarkBasilisk());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new UnderdarkBasilisk());

        levelUpToTwo(rogueClass);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void combatDamageExilesOpponentsTopCardFaceDownWithTheClass() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        Permanent attacker = addCreatureReady(player1, new UnderdarkBasilisk());
        attacker.setAttacking(true);
        Card topCard = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(entry.sourcePermanentId()).isEqualTo(rogueClass.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void levelThreeAllowsCastingExiledCardWithManaOfAnyColor() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        levelUpToThree(rogueClass);
        Permanent attacker = addCreatureReady(player1, new UnderdarkBasilisk());
        attacker.setAttacking(true);
        Card exiledCard = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(exiledCard, new UnderdarkBasilisk(), new UnderdarkBasilisk()));

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void levelOneDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new RogueClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnderdarkBasilisk());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void cannotSkipLevelTwo() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(rogueClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotLevelUpDuringCombat() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        prepareForSorcery();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(rogueClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachCreatureDealingCombatDamageExilesOneCard() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        Card first = new UnderdarkBasilisk();
        Card second = new UnderdarkBasilisk();
        Card third = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(first, second, third));

        resolveCombatAndTrigger();

        assertThat(gd.getExiledWithPermanentEntries(rogueClass.getId(), rogueClass.getCard().getId()))
                .extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
    }

    @Test
    void cannotCastExiledCardsBeforeLevelThree() {
        harness.addToBattlefield(player1, new RogueClass());
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        Card card = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(card, new UnderdarkBasilisk()));
        resolveCombatAndTrigger();
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void levelThreeCanPlayAnExiledLand() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        levelUpToThree(rogueClass);
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land, new UnderdarkBasilisk()));
        resolveCombatAndTrigger();
        prepareForSorcery();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void levelingAnotherClassDoesNotUnlockCardsExiledWithTheFirst() {
        harness.addToBattlefield(player1, new RogueClass());
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        Card card = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(card, new UnderdarkBasilisk()));
        resolveCombatAndTrigger();
        Permanent otherClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        levelUpToThree(otherClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerCanStillLookAtExiledCardAfterClassLeaves() throws Exception {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        Card card = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(card, new UnderdarkBasilisk()));
        resolveCombatAndTrigger();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rogueClass);
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage controllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(controllerState.lookedAtExileCards()).extracting(view -> view.id())
                .contains(card.getId());
        assertThat(opponentState.lookedAtExileCards()).extracting(view -> view.id())
                .doesNotContain(card.getId());
    }

    @Test
    void reachingLevelThreeUnlocksCardsExiledAtLevelOneAndKeepsMenace() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        Permanent attacker = addCreatureReady(player1, new UnderdarkBasilisk());
        attacker.setAttacking(true);
        Card card = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(card, new UnderdarkBasilisk()));
        resolveCombatAndTrigger();

        levelUpToThree(rogueClass);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void losingLevelThreeClassRemovesCastingPermission() {
        Permanent rogueClass = harness.addToBattlefieldAndReturn(player1, new RogueClass());
        levelUpToThree(rogueClass);
        addCreatureReady(player1, new UnderdarkBasilisk()).setAttacking(true);
        Card card = new UnderdarkBasilisk();
        harness.setLibrary(player2, List.of(card, new UnderdarkBasilisk()));
        resolveCombatAndTrigger();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rogueClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private void levelUpToTwo(Permanent rogueClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(rogueClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent rogueClass) {
        levelUpToTwo(rogueClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(rogueClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
