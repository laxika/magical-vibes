package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeamPennant.class, EagerFirstYear.class})
class TeamPennantTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and vigilance and trample")
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new EagerFirstYear());
        Permanent pennant = addPennantReady(player1);
        pennant.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip creature token {1} attaches Team Pennant to a creature token")
    void tokenEquipAttachesToCreatureToken() {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(token.getId());
    }

    @Test
    @DisplayName("Equip creature token {1} cannot target a nontoken creature")
    void tokenEquipRejectsNontokenCreature() {
        Permanent pennant = addPennantReady(player1);
        Permanent creature = addCreatureReady(player1, new EagerFirstYear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature token you control");
        assertThat(pennant.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} attaches Team Pennant to a nontoken creature")
    void regularEquipAttachesToNontokenCreature() {
        Permanent pennant = addPennantReady(player1);
        Permanent creature = addCreatureReady(player1, new EagerFirstYear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(creature.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothEquipAbilitiesRejectOpposingCreatureTokens(int abilityIndex) {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pennant.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothEquipAbilitiesRejectActivationOutsideMainPhase(int abilityIndex) {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(pennant.getAttachedTo()).isNull();
    }

    @Test
    void regularEquipCanAttachToCreatureToken() {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, token.getId());
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(token.getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void reequippingMovesBoostAndKeywordsToNewCreature() {
        Permanent pennant = addPennantReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new EagerFirstYear());
        Permanent token = addTokenCreature(player1);
        pennant.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(token.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void equipDoesNotMoveWhenTargetChangesControllerBeforeResolution(int abilityIndex) {
        Permanent pennant = addPennantReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new EagerFirstYear());
        Permanent token = addTokenCreature(player1);
        pennant.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, abilityIndex, null, token.getId());
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);
        harness.passBothPriorities();

        assertThat(pennant.getAttachedTo()).isEqualTo(oldCreature.getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothEquipAbilitiesRejectActivationWithNonemptyStack(int abilityIndex) {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, abilityIndex, null, token.getId());
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(pennant.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(pennant.getAttachedTo()).isEqualTo(token.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothEquipAbilitiesRejectActivationDuringOpponentsTurn(int abilityIndex) {
        Permanent pennant = addPennantReady(player1);
        Permanent token = addTokenCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(pennant.getAttachedTo()).isNull();
    }

    @Test
    void tokenEquipRejectsNoncreatureToken() {
        Permanent pennant = addPennantReady(player1);
        TeamPennant tokenCard = new TeamPennant();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pennant.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPennantReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TeamPennant());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addTokenCreature(Player player) {
        EagerFirstYear tokenCard = new EagerFirstYear();
        tokenCard.setToken(true);
        return addCreatureReady(player, tokenCard);
    }
}
