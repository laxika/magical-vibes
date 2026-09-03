package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Foxfire.class, BalduvianBears.class, ZuranSpellcaster.class})
class FoxfireTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target attacking creature")
    void untapsTargetAttacker() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();

        castFoxfire(attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a player")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castFoxfire(attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature by a blocker")
    void preventsCombatDamageDealtToCreature() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 0);

        castFoxfire(attacker);
        resolveCombat();

        // A 3/3 blocker would normally kill the 2/2 attacker; combat damage to it is prevented.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertNotInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Prevents combat damage only for the targeted creature")
    void preventsCombatDamageOnlyForTarget() {
        harness.setLife(player2, 20);
        Permanent targetAttacker = addAttacker(player1, player2, 2, 2);
        addAttacker(player1, player2, 2, 2);

        castFoxfire(targetAttacker);
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Noncombat damage to the target creature still applies")
    void noncombatDamageStillAppliesToTargetCreature() {
        Permanent attacker = addAttacker(player1, player2, 3, 3);
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        castFoxfire(attacker);

        int spellcasterIndex = gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster);
        harness.activateAbility(player2, spellcasterIndex, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Schedules a draw at the next turn's upkeep")
    void drawsCardAtNextUpkeep() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castFoxfire(attacker);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent bystander = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Foxfire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by an opponent")
    void canTargetOpponentsAttacker() {
        Permanent attacker = addAttacker(player2, player1, 2, 2);
        attacker.tap();

        castFoxfire(attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    private void castFoxfire(Permanent target) {
        harness.setHand(player1, List.of(new Foxfire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card card = new BalduvianBears().createRuntimeCopy();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card card = new BalduvianBears().createRuntimeCopy();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, card);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        return perm;
    }
}
