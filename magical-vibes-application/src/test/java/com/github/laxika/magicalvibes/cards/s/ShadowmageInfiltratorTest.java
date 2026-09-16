package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PatchworkGnomes;
import com.github.laxika.magicalvibes.cards.p.PsionicGift;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowmageInfiltrator.class, Forest.class, Werebear.class, PatchworkGnomes.class, DuskImp.class,
        PsionicGift.class})
class ShadowmageInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers to draw a card")
    void combatDamageOffersToDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addAttackingShadowmage();

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the combat-damage draw leaves the library unchanged")
    void decliningCombatDamageDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addAttackingShadowmage();

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger the combat-damage draw")
    void noncombatDamageDoesNotTriggerDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent shadowmage = addCreatureReady(player1, new ShadowmageInfiltrator());
        attachPsionicGift(shadowmage);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Fear prevents a nonblack nonartifact creature from blocking")
    void fearPreventsIllegalBlock() {
        Permanent shadowmage = addAttackingShadowmage();
        Permanent blocker = addCreatureReady(player2, new Werebear());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(shadowmage)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        Permanent shadowmage = addAttackingShadowmage();
        Permanent blocker = addCreatureReady(player2, new PatchworkGnomes());

        declareBlocker(blocker, shadowmage);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        Permanent shadowmage = addAttackingShadowmage();
        Permanent blocker = addCreatureReady(player2, new DuskImp());

        declareBlocker(blocker, shadowmage);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttackingShadowmage() {
        Permanent shadowmage = addCreatureReady(player1, new ShadowmageInfiltrator());
        shadowmage.setAttacking(true);
        return shadowmage;
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private void attachPsionicGift(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(creature.getId());
    }
}
