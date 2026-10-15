package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulNova.class, AlphaMyr.class, LeoninScimitar.class, Arrest.class})
class SoulNovaTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the attacking creature and all Equipment attached to it")
    void exilesAttackerAndAttachedEquipment() {
        Permanent attacker = addAttacker(player1);

        Permanent playerEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        playerEquipment.setAttachedTo(attacker.getId());

        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        opponentEquipment.setAttachedTo(attacker.getId());

        harness.addToBattlefield(player2, new LeoninScimitar());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Arrest());
        aura.setAttachedTo(attacker.getId());

        prepareSoulNova();
        harness.castAndResolveInstant(player2, 0, attacker.getId());

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        assertThat(gameData.exiledCards)
                .filteredOn(exiled -> exiled.card().getName().equals("Alpha Myr"))
                .hasSize(1);
        assertThat(gameData.exiledCards)
                .filteredOn(exiled -> exiled.card().getName().equals("Leonin Scimitar"))
                .hasSize(2);
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Arrest");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent attacker = addAttacker(player1);
        Permanent nonAttacker = addCreatureReady(player1, new AlphaMyr());

        prepareSoulNova();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(attacker, nonAttacker);
    }

    @Test
    @DisplayName("Does not resolve if the target stops attacking before resolution")
    void doesNotResolveIfTargetStopsAttacking() {
        Permanent attacker = addAttacker(player1);

        castSoulNova(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpha Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Alpha Myr"));
    }

    @Test
    @DisplayName("Leaves attached Equipment untouched when the target becomes illegal")
    void leavesEquipmentWhenTargetStopsAttacking() {
        Permanent attacker = addAttacker(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(attacker.getId());

        castSoulNova(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        assertThat(equipment.getAttachedTo()).isEqualTo(attacker.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("Exiles an attacking creature with no Equipment")
    void exilesUnequippedAttacker() {
        Permanent attacker = addAttacker(player1);

        prepareSoulNova();
        harness.castAndResolveInstant(player2, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(attacker.getCard());
        harness.assertInGraveyard(player2, "Soul Nova");
    }

    @Test
    @DisplayName("Uses Equipment attached at resolution rather than at casting")
    void usesAttachmentsAtResolution() {
        Permanent attacker = addAttacker(player1);
        Permanent otherCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent movedAway = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        movedAway.setAttachedTo(attacker.getId());
        Permanent movedOnto = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        movedOnto.setAttachedTo(otherCreature.getId());

        castSoulNova(attacker.getId());
        movedAway.setAttachedTo(otherCreature.getId());
        movedOnto.setAttachedTo(attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(otherCreature, movedAway)
                .doesNotContain(attacker, movedOnto);
        assertThat(movedAway.getAttachedTo()).isEqualTo(otherCreature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(attacker.getCard(), movedOnto.getCard());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new AlphaMyr());
        attacker.setAttacking(true);
        return attacker;
    }

    private void castSoulNova(UUID targetId) {
        prepareSoulNova();
        harness.castInstant(player2, 0, targetId);
    }

    private void prepareSoulNova() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new SoulNova()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player2);
    }
}
